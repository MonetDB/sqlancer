package sqlancer.monet.oracle.tlp;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.TestOracle;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetJoin;
import sqlancer.monet.ast.MonetPostfixOperation;
import sqlancer.monet.ast.MonetPostfixOperation.PostfixOperator;
import sqlancer.monet.ast.MonetPrefixOperation;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.gen.MonetCommon;
import sqlancer.monet.gen.MonetExpressionGenerator;
import sqlancer.monet.oracle.MonetNoRECOracle;

public class MonetTLPBase implements TestOracle {

    final MonetGlobalState state;
    final Set<String> errors = new HashSet<>();

    MonetSchema s;
    MonetTables targetTables;
    MonetExpressionGenerator gen;
    MonetSelect select;
    MonetExpression predicate;
    MonetPrefixOperation negatedPredicate;
    MonetPostfixOperation isNullPredicate;

    public MonetTLPBase(MonetGlobalState state) {
        this.state = state;
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonFetchErrors(errors);
    }

    @Override
    public void check() throws SQLException {
        s = state.getSchema();
        targetTables = s.getRandomTableNonEmptyTables();
        gen = new MonetExpressionGenerator(state).setColumns(targetTables.getColumns());
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
        predicate = generatePredicate();
        negatedPredicate = new MonetPrefixOperation(predicate, MonetPrefixOperation.PrefixOperator.NOT);
        isNullPredicate = new MonetPostfixOperation(predicate, PostfixOperator.IS_NULL);
    }

    List<MonetExpression> generateFetchColumns() {
        return Arrays.asList(new MonetColumnValue(targetTables.getColumns().get(0), null));
    }

    MonetExpression generatePredicate() {
        return gen.generateExpression(MonetDataType.BOOLEAN);
    }

}