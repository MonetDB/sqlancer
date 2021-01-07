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
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetJoin;
import sqlancer.monet.ast.MonetQuery;
import sqlancer.monet.ast.MonetQuery.MonetSubquery;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.gen.MonetCommon;
import sqlancer.monet.gen.MonetExpressionGenerator;
import sqlancer.monet.gen.MonetRandomQueryGenerator;
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
        List<MonetExpression> tableList = tables.stream().map(t -> new MonetFromTable(t, Randomly.getBoolean(), null))
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
        MonetQuery select = MonetRandomQueryGenerator.createRandomQuery(0, Randomly.smallNumber() + 1, globalState,
                tables, false, false, false);
        return new MonetSubquery(select, name, null);
    }

}
