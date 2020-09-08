package sqlancer.monet.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.common.oracle.NoRECBase;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.common.query.SQLancerResultSet;
import sqlancer.monet.MonetCompoundDataType;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetCastOperation;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetJoin;
import sqlancer.monet.ast.MonetJoin.MonetJoinType;
import sqlancer.monet.ast.MonetPostfixText;
import sqlancer.monet.ast.MonetQuery.MonetSubquery;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSelect.SelectType;
import sqlancer.monet.gen.MonetCommon;
import sqlancer.monet.gen.MonetExpressionGenerator;
import sqlancer.monet.oracle.tlp.MonetTLPBase;

public class MonetNoRECOracle extends NoRECBase<MonetGlobalState> {

    private final MonetSchema s;

    public MonetNoRECOracle(MonetGlobalState globalState) {
        super(globalState);
        this.s = globalState.getSchema();
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonFetchErrors(errors);
    }

    @Override
    public void check() throws SQLException {
        MonetTables randomTables = s.getRandomTableNonEmptyTables();
        List<MonetColumn> columns = randomTables.getColumns();
        MonetExpression randomWhereCondition = getRandomWhereCondition(columns);
        List<MonetTable> tables = randomTables.getTables();

        List<MonetJoin> joinStatements = getJoinStatements(state, columns, tables);
        List<MonetExpression> fromTables = tables.stream().map(t -> new MonetFromTable(t, Randomly.getBoolean()))
                .collect(Collectors.toList());
        int secondCount = getUnoptimizedQueryCount(fromTables, randomWhereCondition, joinStatements);
        int firstCount = getOptimizedQueryCount(fromTables, columns, randomWhereCondition, joinStatements);
        if (firstCount == -1 || secondCount == -1) {
            throw new IgnoreMeException();
        }
        if (firstCount != secondCount) {
            String queryFormatString = "-- %s;\n-- count: %d";
            String firstQueryStringWithCount = String.format(queryFormatString, optimizedQueryString, firstCount);
            String secondQueryStringWithCount = String.format(queryFormatString, unoptimizedQueryString, secondCount);
            state.getState().getLocalState()
                    .log(String.format("%s\n%s", firstQueryStringWithCount, secondQueryStringWithCount));
            String assertionMessage = String.format("the counts mismatch (%d and %d)!\n%s\n%s", firstCount, secondCount,
                    firstQueryStringWithCount, secondQueryStringWithCount);
            throw new AssertionError(assertionMessage);
        }
    }

    public static List<MonetJoin> getJoinStatements(MonetGlobalState globalState, List<MonetColumn> columns,
            List<MonetTable> tables) {
        List<MonetJoin> joinStatements = new ArrayList<>();
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState).setColumns(columns);
        for (int i = 1; i < tables.size(); i++) {
            MonetExpression joinClause = gen.generateExpression(MonetDataType.BOOLEAN);
            MonetTable table = Randomly.fromList(tables);
            tables.remove(table);
            MonetJoinType options = MonetJoinType.getRandom();
            MonetJoin j = new MonetJoin(new MonetFromTable(table, Randomly.getBoolean()), joinClause, options);
            joinStatements.add(j);
        }
        // JOIN subqueries
        for (int i = 0; i < Randomly.smallNumber(); i++) {
            MonetTables subqueryTables = globalState.getSchema().getRandomTableNonEmptyTables();
            MonetSubquery subquery = MonetTLPBase.createSubquery(globalState, String.format("sub%d", i),
                    subqueryTables);
            MonetExpression joinClause = gen.generateExpression(MonetDataType.BOOLEAN);
            MonetJoinType options = MonetJoinType.getRandom();
            MonetJoin j = new MonetJoin(subquery, joinClause, options);
            joinStatements.add(j);
        }
        return joinStatements;
    }

    private MonetExpression getRandomWhereCondition(List<MonetColumn> columns) {
        return new MonetExpressionGenerator(state).setColumns(columns).generateExpression(MonetDataType.BOOLEAN);
    }

    private int getUnoptimizedQueryCount(List<MonetExpression> fromTables, MonetExpression randomWhereCondition,
            List<MonetJoin> joinStatements) throws SQLException {
        MonetSelect select = new MonetSelect();
        MonetCastOperation isTrue = new MonetCastOperation(randomWhereCondition,
                MonetCompoundDataType.create(MonetDataType.INT));
        MonetPostfixText asText = new MonetPostfixText(isTrue, " as count", null, MonetDataType.INT);
        select.setFetchColumns(Arrays.asList(asText));
        select.setFromList(fromTables);
        select.setSelectType(SelectType.ALL);
        select.setJoinClauses(joinStatements);
        int secondCount = 0;
        unoptimizedQueryString = "SELECT CAST(SUM(count) AS BIGINT) FROM (" + MonetVisitor.asString(select) + ") as res";
        if (options.logEachSelect()) {
            logger.writeCurrent(unoptimizedQueryString);
        }
        errors.add("canceling statement due to statement timeout");
        Query q = new QueryAdapter(unoptimizedQueryString, errors);
        SQLancerResultSet rs;
        try {
            rs = q.executeAndGet(state);
        } catch (Exception e) {
            throw new AssertionError(unoptimizedQueryString, e);
        }
        if (rs == null) {
            return -1;
        }
        if (rs.next()) {
            secondCount += rs.getLong(1);
        }
        rs.close();
        return secondCount;
    }

    private int getOptimizedQueryCount(List<MonetExpression> randomTables, List<MonetColumn> columns,
            MonetExpression randomWhereCondition, List<MonetJoin> joinStatements) throws SQLException {
        MonetSelect select = new MonetSelect();
        MonetColumnValue allColumns = new MonetColumnValue(Randomly.fromList(columns), null);
        select.setFetchColumns(Arrays.asList(allColumns));
        select.setFromList(randomTables);
        select.setWhereClause(randomWhereCondition);
        if (Randomly.getBooleanWithSmallProbability()) {
            select.setOrderByExpressions(new MonetExpressionGenerator(state).setColumns(columns).generateOrderBy());
        }
        select.setSelectType(SelectType.ALL);
        select.setJoinClauses(joinStatements);
        int firstCount = 0;
        try (Statement stat = con.createStatement()) {
            optimizedQueryString = MonetVisitor.asString(select);
            if (options.logEachSelect()) {
                logger.writeCurrent(optimizedQueryString);
            }
            try (ResultSet rs = stat.executeQuery(optimizedQueryString)) {
                while (rs.next()) {
                    firstCount++;
                }
            }
        } catch (SQLException e) {
            throw new IgnoreMeException();
        }
        return firstCount;
    }

}
