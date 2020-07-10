package sqlancer.monet.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sqlancer.IgnoreMeException;
import sqlancer.Main.StateLogger;
import sqlancer.MainOptions;
import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.StateToReproduce.MonetStateToReproduce;
import sqlancer.TestOracle;
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
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSelect.SelectType;
import sqlancer.monet.gen.MonetCommon;
import sqlancer.monet.gen.MonetExpressionGenerator;

public class MonetNoRECOracle implements TestOracle {

    private final MonetSchema s;
    private final Connection con;
    private final MonetStateToReproduce state;
    private String firstQueryString;
    private String secondQueryString;
    private final StateLogger logger;
    private final MainOptions options;
    private final Set<String> errors = new HashSet<>();
    private final MonetGlobalState globalState;

    public MonetNoRECOracle(MonetGlobalState globalState) {
        this.s = globalState.getSchema();
        this.con = globalState.getConnection();
        this.state = (MonetStateToReproduce) globalState.getState();
        this.logger = globalState.getLogger();
        this.options = globalState.getOptions();
        this.globalState = globalState;
    }

    @Override
    public void check() throws SQLException {
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonFetchErrors(errors);
        MonetTables randomTables = s.getRandomTableNonEmptyTables();
        List<MonetColumn> columns = randomTables.getColumns();
        MonetExpression randomWhereCondition = getRandomWhereCondition(columns);
        List<MonetTable> tables = randomTables.getTables();

        List<MonetJoin> joinStatements = getJoinStatements(globalState, columns, tables);
        List<MonetExpression> fromTables = tables.stream().map(t -> new MonetFromTable(t, Randomly.getBoolean()))
                .collect(Collectors.toList());
        int secondCount = getUnoptimizedQueryCount(fromTables, randomWhereCondition, joinStatements);
        int firstCount = getOptimizedQueryCount(fromTables, columns, randomWhereCondition, joinStatements);
        if (firstCount == -1 || secondCount == -1) {
            throw new IgnoreMeException();
        }
        if (firstCount != secondCount) {
            state.queryString = firstCount + " " + secondCount + " " + firstQueryString + ";\n" + secondQueryString
                    + ";";
            throw new AssertionError(firstQueryString + secondQueryString + firstCount + " " + secondCount);
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
            MonetJoin j = new MonetJoin(table, joinClause, options);
            joinStatements.add(j);
        }
        return joinStatements;
    }

    private MonetExpression getRandomWhereCondition(List<MonetColumn> columns) {
        return new MonetExpressionGenerator(globalState).setColumns(columns).setGlobalState(globalState)
                .generateExpression(MonetDataType.BOOLEAN);
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
        secondQueryString = "SELECT SUM(count) FROM (" + MonetVisitor.asString(select) + ") as res";
        if (options.logEachSelect()) {
            logger.writeCurrent(secondQueryString);
        }
        errors.add("canceling statement due to statement timeout");
        Query q = new QueryAdapter(secondQueryString, errors);
        ResultSet rs;
        try {
            rs = q.executeAndGet(globalState);
        } catch (Exception e) {
            throw new AssertionError(secondQueryString, e);
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
            select.setOrderByExpressions(new MonetExpressionGenerator(globalState).setColumns(columns)
                    .setGlobalState(globalState).generateOrderBy());
        }
        select.setSelectType(SelectType.ALL);
        select.setJoinClauses(joinStatements);
        int firstCount = 0;
        try (Statement stat = con.createStatement()) {
            firstQueryString = MonetVisitor.asString(select);
            if (options.logEachSelect()) {
                logger.writeCurrent(firstQueryString);
            }
            try (ResultSet rs = stat.executeQuery(firstQueryString)) {
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
