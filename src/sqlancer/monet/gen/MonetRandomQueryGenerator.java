package sqlancer.monet.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSelect.SelectType;

public final class MonetRandomQueryGenerator {

    private MonetRandomQueryGenerator() {
    }

    private static MonetSelect createRandomQueryInternal(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, List<MonetExpression> columns, boolean generateOrderBy, boolean generateLimit) {
        MonetSelect select = new MonetSelect();
        select.setSelectType(SelectType.getRandom());
        if (select.getSelectOption() == SelectType.DISTINCT && Randomly.getBoolean()) {
            select.setDistinctOnClause(gen.generateExpression(0));
        }
        select.setFromList(tables.getTables().stream().map(t -> new MonetFromTable(t, Randomly.getBoolean()))
                .collect(Collectors.toList()));
        select.setFetchColumns(columns);
        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression(0, MonetDataType.BOOLEAN));
        }
        if (Randomly.getBooleanWithRatherLowProbability()) {
            select.setGroupByExpressions(gen.generateExpressions(Randomly.smallNumber() + 1));
            if (Randomly.getBoolean()) {
                select.setHavingClause(gen.generateHavingClause());
            }
        }
        if (generateOrderBy && Randomly.getBooleanWithRatherLowProbability()) {
            select.setOrderByExpressions(gen.generateOrderBy());
        }
        if (generateLimit && Randomly.getBoolean()) {
            select.setLimitClause(MonetConstant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger()));
            if (Randomly.getBoolean()) {
                select.setOffsetClause(
                        MonetConstant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger()));
            }
        }
        return select;
    }

    public static MonetSelect createRandomQuery(int nrColumns, MonetGlobalState globalState, boolean generateOrderBy, boolean generateLimit) {
        List<MonetExpression> columns = new ArrayList<>();
        MonetTables tables = globalState.getSchema().getRandomTableNonEmptyTables();
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState).setColumns(tables.getColumns());
        for (int i = 0; i < nrColumns; i++) {
            columns.add(gen.generateExpression(0));
        }
        return createRandomQueryInternal(gen, globalState, tables, columns, generateOrderBy, generateLimit);
    }

    public static MonetSelect createRandomSingleColumnQuery(int depth, MonetDataType tp, MonetGlobalState globalState, boolean generateOrderBy, boolean generateLimit) {
        List<MonetExpression> columns = new ArrayList<>();
        MonetTables tables = globalState.getSchema().getRandomTableNonEmptyTables();
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState).setColumns(tables.getColumns());
        columns.add(gen.generateExpression(depth, tp));
        return createRandomQueryInternal(gen, globalState, tables, columns, generateOrderBy, generateLimit);
    }

}
