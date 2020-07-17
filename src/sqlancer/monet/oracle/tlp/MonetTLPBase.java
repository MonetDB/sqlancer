package sqlancer.monet.oracle.tlp;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.TernaryLogicPartitioningOracleBase;
import sqlancer.TestOracle;
import sqlancer.gen.ExpressionGenerator;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetJoin;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.gen.MonetCommon;
import sqlancer.monet.gen.MonetExpressionGenerator;
import sqlancer.monet.oracle.MonetNoRECOracle;

public class MonetTLPBase extends TernaryLogicPartitioningOracleBase<MonetExpression, MonetGlobalState>
        implements TestOracle {

    MonetSchema s;
    MonetTables targetTables;
    MonetExpressionGenerator gen;
    MonetSelect select;

    public MonetTLPBase(MonetGlobalState state) {
        super(state);
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonFetchErrors(errors);
    }

    @Override
    public void check() throws SQLException {
        // clear left-over query string from previous test
        state.getState().queryString = null;
        s = state.getSchema();
        targetTables = s.getRandomTableNonEmptyTables();
        gen = new MonetExpressionGenerator(state).setColumns(targetTables.getColumns());
        initializeTernaryPredicateVariants();
        select = new MonetSelect();
        select.setFetchColumns(generateFetchColumns());
        List<MonetTable> tables = targetTables.getTables();
        List<MonetJoin> joins = MonetNoRECOracle.getJoinStatements(state, targetTables.getColumns(), tables);
        List<MonetExpression> tableList = tables.stream().map(t -> new MonetFromTable(t, Randomly.getBoolean()))
                .collect(Collectors.toList());
        // TODO joins
        select.setFromList(tableList);
        select.setWhereClause(null);
        select.setJoinClauses(joins);
    }

    List<MonetExpression> generateFetchColumns() {
        return Arrays.asList(new MonetColumnValue(targetTables.getColumns().get(0), null));
    }

    @Override
    protected ExpressionGenerator<MonetExpression> getGen() {
        return gen;
    }

}
