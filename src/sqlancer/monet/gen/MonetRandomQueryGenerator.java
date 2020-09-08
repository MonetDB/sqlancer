package sqlancer.monet.gen;

import java.util.ArrayList;
import java.util.Arrays;
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
import sqlancer.monet.ast.MonetSet;
import sqlancer.monet.ast.MonetSet.SetDistictOrAll;
import sqlancer.monet.ast.MonetSet.SetType;
import sqlancer.monet.ast.MonetQuery;

public final class MonetRandomQueryGenerator {

    private MonetRandomQueryGenerator() {
    }

    private static MonetQuery createSelect(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, MonetSelect select, List<MonetExpression> columns, boolean generateOrderBy, boolean generateLimit) {
        select.setSelectType(SelectType.getRandom());
        select.setFromList(tables.getTables().stream().map(t -> new MonetFromTable(t, Randomly.getBoolean())).collect(Collectors.toList()));
        select.setFetchColumns(columns);
        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression(depth, MonetDataType.BOOLEAN));
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
                select.setOffsetClause(MonetConstant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger()));
            }
        }
        return select;
    }

    private static MonetQuery createSimpleSelect(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, int nrColumns, boolean generateOrderBy, boolean generateLimit) {
        MonetSelect select = new MonetSelect();
        List<MonetExpression> columns = new ArrayList<>();
        for (int i = 0; i < nrColumns; i++) {
            columns.add(gen.generateExpression(depth));
        }
        return createSelect(gen, globalState, tables, depth, select, columns, generateOrderBy, generateLimit);
    }

    private static MonetQuery createSet(MonetExpressionGenerator gen, MonetGlobalState globalState, int depth, int nrColumns) {
        MonetTables tl = globalState.getSchema().getRandomTableNonEmptyTables();
        gen.setColumns(tl.getColumns());
        MonetQuery left = createSimpleSelect(gen, globalState, tl, depth, nrColumns, false, false);

        MonetTables tr = globalState.getSchema().getRandomTableNonEmptyTables();
        gen.setColumns(tr.getColumns());
        MonetQuery right = createSimpleSelect(gen, globalState, tr, depth, nrColumns, false, false);
        return new MonetSet(Randomly.fromList(Arrays.asList(SetType.values())), Randomly.fromList(Arrays.asList(SetDistictOrAll.values())), left, right);
    }

    private static MonetQuery createRandomQueryInternal(MonetExpressionGenerator gen, MonetGlobalState globalState, int depth, int nrColumns, boolean generateOrderBy, boolean generateLimit) {
        if (Randomly.getBoolean()) {
            MonetTables tables = globalState.getSchema().getRandomTableNonEmptyTables();
            gen.setColumns(tables.getColumns());
            return createSimpleSelect(gen, globalState, tables, depth, nrColumns, generateOrderBy, generateLimit);
        } else {
            return createSet(gen, globalState, depth, nrColumns);
        }
    }

    public static MonetQuery createRandomQuery(int depth, int nrColumns, MonetGlobalState globalState, boolean generateOrderBy, boolean generateLimit, boolean allowParameters) {
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState);
        gen.setAllowParameters(allowParameters);
        return createRandomQueryInternal(gen, globalState, depth, nrColumns, generateOrderBy, generateLimit);
    }

    /* Single column cases................. */

    private static MonetQuery createSingleColumnSelect(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, MonetDataType tp, boolean generateOrderBy, boolean generateLimit) {
        MonetSelect select = new MonetSelect();
        List<MonetExpression> columns = new ArrayList<>();
        columns.add(gen.generateExpression(depth, tp));
        return createSelect(gen, globalState, tables, depth, select, columns, generateOrderBy, generateLimit);
    }

    private static MonetQuery createSingleColumnSet(MonetExpressionGenerator gen, MonetGlobalState globalState, int depth, MonetDataType tp) {
        MonetTables tl = globalState.getSchema().getRandomTableNonEmptyTables();
        gen.setColumns(tl.getColumns());
        MonetQuery left = createSingleColumnSelect(gen, globalState, tl, depth, tp, false, false);

        MonetTables tr = globalState.getSchema().getRandomTableNonEmptyTables();
        gen.setColumns(tr.getColumns());
        MonetQuery right = createSingleColumnSelect(gen, globalState, tr, depth, tp, false, false);
        return new MonetSet(Randomly.fromList(Arrays.asList(SetType.values())), Randomly.fromList(Arrays.asList(SetDistictOrAll.values())), left, right);
    }

    private static MonetQuery createRandomSingleColumnQueryInternal(MonetExpressionGenerator gen, MonetGlobalState globalState, int depth, MonetDataType tp, boolean generateOrderBy, boolean generateLimit) {
        if (Randomly.getBoolean()) {
            MonetTables tables = globalState.getSchema().getRandomTableNonEmptyTables();
            gen.setColumns(tables.getColumns());
            return createSingleColumnSelect(gen, globalState, tables, depth, tp, generateOrderBy, generateLimit);
        } else {
            return createSingleColumnSet(gen, globalState, depth, tp);
        }
    }

    public static MonetQuery createRandomSingleColumnQuery(int depth, MonetDataType tp, MonetGlobalState globalState, boolean generateOrderBy, boolean generateLimit, boolean allowParameters) {
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState);
        gen.setAllowParameters(allowParameters);
        return createRandomSingleColumnQueryInternal(gen, globalState, depth, tp, generateOrderBy, generateLimit);
    }

}
