package sqlancer.monet.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetJoin;
import sqlancer.monet.ast.MonetJoin.MonetJoinType;
import sqlancer.monet.ast.MonetQuery;
import sqlancer.monet.ast.MonetQuery.MonetSubquery;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetCTE;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSelect.MonetQueryCTE;
import sqlancer.monet.ast.MonetSelect.SelectType;
import sqlancer.monet.ast.MonetSet;
import sqlancer.monet.ast.MonetSet.SetDistictOrAll;
import sqlancer.monet.ast.MonetSet.SetType;
import sqlancer.monet.ast.MonetValues;

public final class MonetRandomQueryGenerator {

    private static final List<MonetDataType> ALL_TYPES = MonetDataType.getAllTypes();

    private static final int MAX_SUBQUERY_DEPTH = 1;

    private MonetRandomQueryGenerator() {
    }

    private static MonetTables getNextBatchOfTables(MonetGlobalState globalState, int depth, boolean generateCTEs) {
        List<MonetTable> databasetables = globalState.getSchema().getRandomTableNonEmptyTablesAsList();
        List<MonetTable> tables = new ArrayList<>(databasetables.size());

        for (MonetTable t : databasetables) {
            List<MonetColumn> cols = new ArrayList<>(t.getColumns().size());
            for (MonetColumn c : t.getColumns()) {
                cols.add(new MonetColumn(c.getName(), c.getType(), String.format("l%d%s", depth, t.getName())));
            }
            tables.add(new MonetTable(t.getName(), cols, t.getIndexes(), t.getTableType(), t.getStatistics(), t.getConstraints(),
                    t.isView(), t.isInsertable()));
        }

        if (generateCTEs && depth < MAX_SUBQUERY_DEPTH) { /* Protect against infinite recursion */
            for (int i = 0; i < Randomly.smallNumber(); i++) {
                String nextName = String.format("cte%d", i);
                MonetQuery q = createRandomQuery(depth + 1, Randomly.smallNumber() + 1, globalState, null, false, false,
                        false);
                List<MonetColumn> cols = new ArrayList<>();

                if (q.getFetchColumns() != null && !q.getFetchColumns().isEmpty()) {
                    int j = 0;
                    for (MonetExpression ex : q.getFetchColumns()) {
                        String nextColumnName = String.format("c%d", j);
                        MonetDataType dt = ex.getExpressionType();
                        if (dt == null) {
                            throw new AssertionError("Ups " + ex.getClass().getName()); /* this is for debugging */
                        }
                        cols.add(new MonetColumn(nextColumnName, dt, String.format("l%d%s", depth, nextName)));
                        j++;
                    }
                }
                tables.add(new MonetCTE(nextName, cols, q));
            }
        }
        return new MonetTables(tables);
    }

    private static MonetQuery createSelect(MonetExpressionGenerator gen, MonetGlobalState globalState,
            MonetTables tables, int depth, List<MonetDataType> types, boolean generateOrderBy, boolean generateLimit,
            boolean setalias) {
        MonetSelect select = new MonetSelect();
        select.setSelectType(SelectType.getRandom());
        boolean groupBy = Randomly.getBooleanWithRatherLowProbability(), areAggregatesAllowed = gen.areAggregatesAllowed();

        if (tables != null && !tables.getTables().isEmpty()) {
            List<MonetExpression> ctes = new ArrayList<>(tables.getTables().size());
            List<MonetExpression> fromList = new ArrayList<>(tables.getTables().size());
            for (MonetTable t : tables.getTables()) {
                if (t instanceof MonetCTE) {
                    MonetCTE cte = (MonetCTE) t;
                    ctes.add(new MonetQueryCTE(cte, cte.getName(),
                            setalias ? String.format("l%d%s", depth, cte.getName()) : null));
                } else {
                    fromList.add(new MonetFromTable(t, Randomly.getBoolean(),
                            setalias ? String.format("l%d%s", depth, t.getName()) : null));
                }
            }
            select.setCTEs(ctes);
            select.setFromList(fromList);
        }

        if (tables != null && !tables.getTables().isEmpty() && Randomly.getBoolean()) {
            List<MonetJoin> joinStatements = new ArrayList<>();

            // JOIN subqueries
            if (depth < MAX_SUBQUERY_DEPTH) { /* Protect against infinite recursion */
                for (int i = 0; i < Randomly.fromOptions(0, 1); i++) {
                    MonetQuery q = createRandomQuery(depth + 1, Randomly.smallNumber() + 1, globalState, null, false,
                            false, false);

                    String name = String.format("sub%d", i);
                    List<MonetColumn> cols = new ArrayList<>();
                    int j = 0;
                    for (MonetExpression ex : q.getFetchColumns()) {
                        String nextColumnName = String.format("c%d", j);
                        MonetDataType dt = ex.getExpressionType();
                        if (dt == null) {
                            throw new AssertionError("Ups " + ex.getClass().getName()); /* this is for debugging */
                        }
                        cols.add(new MonetColumn(nextColumnName, dt, String.format("%s", name)));
                        j++;
                    }
                    MonetSubquery subquery = new MonetSubquery(q, name, null, cols);

                    List<MonetExpression> joinclauses = new ArrayList<>();
                    for (int k = 0; k < Randomly.fromOptions(1, 2); k++) {
                        joinclauses.add(gen.generateExpression(depth + 1, MonetDataType.BOOLEAN));
                    }

                    joinStatements.add(new MonetJoin(subquery, joinclauses, MonetJoinType.getRandom()));
                }
            }
            select.setJoinClauses(joinStatements);
        }

        List<MonetExpression> columns = new ArrayList<>(types.size());
        gen.allowAggregates(groupBy);
        for (MonetDataType tp : types) {
            columns.add(gen.generateExpression(depth, tp));
        }
        gen.allowAggregates(areAggregatesAllowed);
        select.setFetchColumns(columns);

        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression(depth + 1, MonetDataType.BOOLEAN));
        }
        if (groupBy) {
            select.setGroupByExpressions(gen.generateExpressions(Randomly.smallNumber() + 1, depth + 1));
            if (Randomly.getBooleanWithRatherLowProbability()) {
                select.setHavingClause(gen.generateHavingClause(depth + 1));
            }
        }
        if (generateOrderBy && Randomly.getBooleanWithRatherLowProbability()) {
            gen.allowAggregates(groupBy);
            select.setOrderByExpressions(gen.generateOrderBy(depth + 1));
            gen.allowAggregates(areAggregatesAllowed);
        }
        if (generateLimit && Randomly.getBoolean()) {
            select.setLimitClause(
                    MonetConstant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger(), MonetDataType.INT));
            if (Randomly.getBoolean()) {
                select.setOffsetClause(MonetConstant.createIntConstant(Randomly.getPositiveOrZeroNonCachedInteger(),
                        MonetDataType.INT));
            }
        }
        return select;
    }

    public static MonetQuery createSimpleSelect(MonetExpressionGenerator gen, MonetGlobalState globalState,
            MonetTables tables, int depth, List<MonetDataType> types, boolean generateOrderBy, boolean generateLimit,
            boolean generateCTEs) {
        boolean setalias = false;
        MonetTables tb = tables;

        if (tb == null && !Randomly.getBooleanWithRatherLowProbability()) { /* also generate from-less queries */
            tb = getNextBatchOfTables(globalState, depth, generateCTEs);
            setalias = true;
        }
        if (tb != null) {
            gen.setColumns(tb.getColumns());
        }
        return createSelect(gen, globalState, tb, depth, types, generateOrderBy, generateLimit, setalias);
    }

    private static MonetQuery createSet(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables,
            int depth, int nrColumns) {
        List<MonetDataType> types = Randomly.nonEmptySubsetPotentialDuplicates(ALL_TYPES, nrColumns);

        MonetQuery left = createSimpleSelect(gen, globalState, tables, depth, types, false, false, false);
        MonetQuery right = createSimpleSelect(gen, globalState, tables, depth, types, false, false, false);

        MonetQuery res = new MonetSet(Randomly.fromList(Arrays.asList(SetType.values())),
                Randomly.fromList(Arrays.asList(SetDistictOrAll.values())), left, right);
        res.setFetchColumns(left.getFetchColumns());
        return res;
    }

    private static MonetQuery createValues(
            MonetExpressionGenerator gen/*
                                         * , MonetGlobalState globalState , MonetTables tables
                                         */, int depth, int nrColumns) {
        /*
         * if (tables == null) { tables = getNextBatchOfTables(globalState, depth, false); } if (tables != null) {
         * gen.setColumns(tables.getColumns()); }
         */

        List<MonetDataType> types = Randomly.nonEmptySubsetPotentialDuplicates(ALL_TYPES, nrColumns);
        int nrRows = Randomly.smallNumber() + 1;
        List<List<MonetExpression>> rows = new ArrayList<>(nrRows);
        for (int i = 0; i < nrRows; i++) {
            List<MonetExpression> columns = new ArrayList<>(nrColumns);

            for (int j = 0; j < nrColumns; j++) {
                columns.add(gen.generateExpression(depth, types.get(j)));
            }
            rows.add(columns);
        }
        MonetQuery res = new MonetValues(rows);
        res.setFetchColumns(rows.get(0));
        return res;
    }

    private static MonetQuery createRandomQueryInternal(MonetExpressionGenerator gen, MonetGlobalState globalState,
            MonetTables tables, int depth, int nrColumns, boolean generateOrderBy, boolean generateLimit) {
        switch (Randomly.fromOptions(1, 2, 3, 4)) {
        case 1:
        case 2:
            List<MonetDataType> types = Randomly.nonEmptySubsetPotentialDuplicates(ALL_TYPES, nrColumns);
            return createSimpleSelect(gen, globalState, tables, depth, types, generateOrderBy, generateLimit, true);
        case 3:
            return createSet(gen, globalState, tables, depth, nrColumns);
        case 4:
            return createValues(gen/* , globalState , tables */, depth, nrColumns);
        default:
            throw new AssertionError();
        }
    }

    public static MonetQuery createRandomQuery(int depth, int nrColumns, MonetGlobalState globalState,
            MonetTables tables, boolean generateOrderBy, boolean generateLimit, boolean allowParameters) {
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState);
        gen.setAllowParameters(allowParameters);
        return createRandomQueryInternal(gen, globalState, tables, depth, nrColumns, generateOrderBy, generateLimit);
    }

    /* Single column cases................. */

    private static MonetQuery createSingleColumnSelect(MonetExpressionGenerator gen, MonetGlobalState globalState,
            MonetTables tables, int depth, MonetDataType tp, boolean generateOrderBy, boolean generateLimit,
            boolean generateCTEs) {
        boolean setalias = false;
        MonetTables tb = tables;

        if (tb == null && !Randomly.getBooleanWithRatherLowProbability()) { /* also generate from-less queries */
            tb = getNextBatchOfTables(globalState, depth, generateCTEs);
            setalias = true;
        }
        if (tb != null) {
            gen.setColumns(tb.getColumns());
        }

        List<MonetDataType> tps = new ArrayList<>(1);
        tps.add(tp);
        return createSelect(gen, globalState, tb, depth, tps, generateOrderBy, generateLimit, setalias);
    }

    private static MonetQuery createSingleColumnSet(MonetExpressionGenerator gen, MonetGlobalState globalState,
            int depth, MonetDataType tp) {
        MonetQuery left = createSingleColumnSelect(gen, globalState, null, depth, tp, false, false, false);
        MonetQuery right = createSingleColumnSelect(gen, globalState, null, depth, tp, false, false, false);
        return new MonetSet(Randomly.fromList(Arrays.asList(SetType.values())),
                Randomly.fromList(Arrays.asList(SetDistictOrAll.values())), left, right);
        // res.setFetchColumns(left.getFetchColumns());
    }

    private static MonetQuery createSingleColumnValues(MonetExpressionGenerator gen/* , MonetGlobalState globalState */,
            int depth, MonetDataType tp) {
        // MonetTables tables = getNextBatchOfTables(globalState, depth, false);
        // gen.setColumns(tables.getColumns());

        int nrRows = Randomly.smallNumber() + 1;
        List<List<MonetExpression>> rows = new ArrayList<>(nrRows);
        for (int i = 0; i < nrRows; i++) {
            List<MonetExpression> columns = new ArrayList<>(1);

            columns.add(gen.generateExpression(depth, tp));
            rows.add(columns);
        }
        MonetQuery res = new MonetValues(rows);
        res.setFetchColumns(rows.get(0));
        return res;
    }

    private static MonetQuery createRandomSingleColumnQueryInternal(MonetExpressionGenerator gen,
            MonetGlobalState globalState, int depth, MonetDataType tp, boolean generateOrderBy, boolean generateLimit) {
        switch (Randomly.fromOptions(1, 2, 3, 4)) {
        case 1:
        case 2:
            return createSingleColumnSelect(gen, globalState, null, depth, tp, generateOrderBy, generateLimit, true);
        case 3:
            return createSingleColumnSet(gen, globalState, depth, tp);
        case 4:
            return createSingleColumnValues(gen/* , globalState */, depth, tp);
        default:
            throw new AssertionError();
        }
    }

    public static MonetQuery createRandomSingleColumnQuery(int depth, MonetDataType tp, MonetGlobalState globalState,
            boolean generateOrderBy, boolean generateLimit, boolean allowParameters) {
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState);
        gen.setAllowParameters(allowParameters);
        return createRandomSingleColumnQueryInternal(gen, globalState, depth, tp, generateOrderBy, generateLimit);
    }

}
