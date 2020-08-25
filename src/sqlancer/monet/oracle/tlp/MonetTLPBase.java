package sqlancer.monet.oracle.tlp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.common.gen.ExpressionGenerator;
import sqlancer.common.oracle.TernaryLogicPartitioningOracleBase;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetJoin;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSelect.MonetSubquery;
import sqlancer.monet.gen.MonetCommon;
import sqlancer.monet.gen.MonetExpressionGenerator;
import sqlancer.monet.oracle.MonetNoRECOracle;

public class MonetTLPBase extends TernaryLogicPartitioningOracleBase<MonetExpression, MonetGlobalState> {

    protected MonetSchema s;
    protected MonetTables targetTables;
    protected MonetExpressionGenerator gen;
    protected MonetSelect select;

    public MonetTLPBase(MonetGlobalState state) {
        super(state);
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonFetchErrors(errors);
    }

    @Override
    public void check() throws SQLException {
        s = state.getSchema();
        targetTables = s.getRandomTableNonEmptyTables();
        List<MonetTable> tables = targetTables.getTables();
        List<MonetJoin> joins = getJoinStatements(state, targetTables.getColumns(), tables);
        generateSelectBase(tables, joins);
    }

    protected List<MonetJoin> getJoinStatements(MonetGlobalState globalState, List<MonetColumn> columns,
            List<MonetTable> tables) {
        return MonetNoRECOracle.getJoinStatements(state, columns, tables);
        // TODO joins
    }

    protected void generateSelectBase(List<MonetTable> tables, List<MonetJoin> joins) {
        List<MonetExpression> tableList = tables.stream().map(t -> new MonetFromTable(t, Randomly.getBoolean()))
                .collect(Collectors.toList());
        gen = new MonetExpressionGenerator(state).setColumns(targetTables.getColumns());
        initializeTernaryPredicateVariants();
        select = new MonetSelect();
        select.setFetchColumns(generateFetchColumns());
        select.setFromList(tableList);
        select.setWhereClause(null);
        select.setJoinClauses(joins);
    }

    List<MonetExpression> generateFetchColumns() {
        if (Randomly.getBooleanWithRatherLowProbability()) {
            return Arrays.asList(new MonetColumnValue(MonetColumn.createDummy("*"), null));
        }
        List<MonetExpression> fetchColumns = new ArrayList<>();
        List<MonetColumn> targetColumns = Randomly.nonEmptySubset(targetTables.getColumns());
        for (MonetColumn c : targetColumns) {
            fetchColumns.add(new MonetColumnValue(c, null));
        }
        return fetchColumns;
    }

    @Override
    protected ExpressionGenerator<MonetExpression> getGen() {
        return gen;
    }

    public static MonetSubquery createSubquery(MonetGlobalState globalState, String name, MonetTables tables) {
        List<MonetExpression> columns = new ArrayList<>();
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState).setColumns(tables.getColumns());
        for (int i = 0; i < Randomly.smallNumber() + 1; i++) {
            columns.add(gen.generateExpression(0));
        }
        MonetSelect select = new MonetSelect();
        select.setFromList(tables.getTables().stream().map(t -> new MonetFromTable(t, Randomly.getBoolean()))
                .collect(Collectors.toList()));
        select.setFetchColumns(columns);
        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression(0, MonetDataType.BOOLEAN));
        }
        /*We don't support order by and limit on subqueries on purpose
        if (Randomly.getBooleanWithRatherLowProbability()) {
            select.setOrderByExpressions(gen.generateOrderBy());
        }
        if (Randomly.getBoolean()) {
            select.setLimitClause(MonetConstant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger()));
            if (Randomly.getBoolean()) {
                select.setOffsetClause(
                        MonetConstant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger()));
            }
        }*/
        return new MonetSubquery(select, name);
    }

}
