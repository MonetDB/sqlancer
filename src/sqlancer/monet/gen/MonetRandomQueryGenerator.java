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
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSelect.MonetCTE;
import sqlancer.monet.ast.MonetSelect.SelectType;
import sqlancer.monet.ast.MonetSelect.MonetQueryCTE;
import sqlancer.monet.ast.MonetSet;
import sqlancer.monet.ast.MonetSet.SetDistictOrAll;
import sqlancer.monet.ast.MonetSet.SetType;
import sqlancer.monet.ast.MonetQuery;
import sqlancer.monet.ast.MonetQuery.MonetSubquery;
import sqlancer.monet.ast.MonetValues;

public final class MonetRandomQueryGenerator {

    private static final List<MonetDataType> ALL_TYPES = Arrays.asList(MonetDataType.values());

    private static final int MAX_SUBQUERY_DEPTH = 3;

    private MonetRandomQueryGenerator() {
    }

    private static MonetTables getNextBatchOfTables(MonetGlobalState globalState, int depth, boolean generateCTEs) {
        List<MonetTable> tables = globalState.getSchema().getRandomTableNonEmptyTablesAsList();

        if (generateCTEs && depth < MAX_SUBQUERY_DEPTH) { /* Protect against infinite recursion */
            for (int i = 0; i < Randomly.smallNumber(); i++) {
                String nextName = String.format("cte%d", i);
                MonetQuery q = createRandomQuery(depth + 1, Randomly.smallNumber() + 1, globalState, null, false, false, false);
                List<MonetColumn> cols = new ArrayList<>(q.getFetchColumns().size());

                int j = 0;
                for (MonetExpression ex : q.getFetchColumns()) {
                    MonetDataType dt = ex.getExpressionType();
                    if (dt == null)
                        throw new AssertionError("Ups " + ex.getClass().getName());
                    cols.add(new MonetColumn(String.format("c%d", j), dt));
                    j++;
                }
                tables.add(new MonetCTE(nextName, cols, q));
            }
        }
        return new MonetTables(tables);
    }

    private static MonetQuery createSelect(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, MonetSelect select, List<MonetExpression> columns, boolean generateOrderBy, boolean generateLimit) {
        select.setSelectType(SelectType.getRandom());

        if (tables != null && !tables.getTables().isEmpty()) {
            List<MonetExpression> ctes = new ArrayList<>(tables.getTables().size());
            List<MonetExpression> fromList = new ArrayList<>(tables.getTables().size());
            for (MonetTable t : tables.getTables()) {
                if (t instanceof MonetCTE) {
                    MonetCTE cte = (MonetCTE) t;
                    ctes.add(new MonetQueryCTE(cte, cte.getName()));
                } else {
                    fromList.add(new MonetFromTable(t, Randomly.getBoolean()));
                }
            }
            select.setCTEs(ctes);
            select.setFromList(fromList);
        }

        select.setFetchColumns(columns);
        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression(depth + 1, MonetDataType.BOOLEAN));
        }
        if (tables != null && !tables.getTables().isEmpty() && Randomly.getBoolean()) {
            List<MonetJoin> joinStatements = new ArrayList<>();
            List<MonetTable> tablesToJoin = new ArrayList<>(tables.getTables());

            for (int i = 0; i < Randomly.smallNumber() && i < tablesToJoin.size(); i++) {
                MonetTable table = Randomly.fromList(tablesToJoin);
                tablesToJoin.remove(table);
                joinStatements.add(new MonetJoin(new MonetFromTable(table, Randomly.getBoolean()), gen.generateExpression(depth + 1, MonetDataType.BOOLEAN), MonetJoinType.getRandom()));
            }
            // JOIN subqueries
            if (depth < MAX_SUBQUERY_DEPTH) { /* Protect against infinite recursion */
                for (int i = 0; i < Randomly.smallNumber() + 1; i++) {
                    MonetQuery q = createRandomQuery(depth + 1, Randomly.smallNumber() + 1, globalState, null, false, false, false);
                    MonetSubquery subquery = new MonetSubquery(q, String.format("sub%d", i), null);
                    joinStatements.add(new MonetJoin(subquery, gen.generateExpression(depth + 1, MonetDataType.BOOLEAN), MonetJoinType.getRandom()));
                }
            }
            select.setJoinClauses(joinStatements);
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

    private static MonetQuery createSimpleSelect(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, List<MonetDataType> types, boolean generateOrderBy, boolean generateLimit, boolean generateCTEs) {
        if (tables == null && !Randomly.getBoolean()) { /* also generate from-less queries */
            tables = getNextBatchOfTables(globalState, depth, generateCTEs);
        }
        if (tables != null) {
            gen.setColumns(tables.getColumns());
        }

        MonetSelect select = new MonetSelect();
        List<MonetExpression> columns = new ArrayList<>(types.size());
        for (MonetDataType tp : types) {
            columns.add(gen.generateExpression(depth, tp));
        }
        return createSelect(gen, globalState, tables, depth, select, columns, generateOrderBy, generateLimit);
    }

    private static MonetQuery createSet(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, int nrColumns) {
        List<MonetDataType> types = Randomly.nonEmptySubsetPotentialDuplicates(ALL_TYPES, nrColumns);

        MonetQuery left = createSimpleSelect(gen, globalState, tables, depth, types, false, false, false);
        MonetQuery right = createSimpleSelect(gen, globalState, tables, depth, types, false, false, false);

        MonetQuery res = new MonetSet(Randomly.fromList(Arrays.asList(SetType.values())), Randomly.fromList(Arrays.asList(SetDistictOrAll.values())), left, right);
        res.setFetchColumns(left.getFetchColumns());
        return res;
    }

    private static MonetQuery createValues(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, int nrColumns) {
        if (tables == null) {
            tables = getNextBatchOfTables(globalState, depth, false);
        }
        if (tables != null) {
            gen.setColumns(tables.getColumns());
        }

        List<MonetDataType> types = Randomly.nonEmptySubsetPotentialDuplicates(ALL_TYPES, nrColumns);
        int nrRows = Randomly.smallNumber() + 1;
        List<List<MonetExpression>> rows = new ArrayList<>(nrRows);
        for (int i = 0 ; i < nrRows; i++) {
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

    private static MonetQuery createRandomQueryInternal(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, int nrColumns, boolean generateOrderBy, boolean generateLimit) {
        switch (Randomly.fromOptions(1, 2, 3, 4)) {
            case 1:
            case 2:
                List<MonetDataType> types = Randomly.nonEmptySubsetPotentialDuplicates(ALL_TYPES, nrColumns);
                return createSimpleSelect(gen, globalState, tables, depth, types, generateOrderBy, generateLimit, true);
            case 3:
                return createSet(gen, globalState, tables, depth, nrColumns);
            case 4:
                return createValues(gen, globalState, tables, depth, nrColumns);
            default:
                throw new AssertionError();
        }
    }

    public static MonetQuery createRandomQuery(int depth, int nrColumns, MonetGlobalState globalState, MonetTables tables, boolean generateOrderBy, boolean generateLimit, boolean allowParameters) {
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState);
        gen.setAllowParameters(allowParameters);
        return createRandomQueryInternal(gen, globalState, tables, depth, nrColumns, generateOrderBy, generateLimit);
    }

    /* Single column cases................. */

    private static MonetQuery createSingleColumnSelect(MonetExpressionGenerator gen, MonetGlobalState globalState, MonetTables tables, int depth, MonetDataType tp, boolean generateOrderBy, boolean generateLimit, boolean generateCTEs) {
        if (tables == null) {
            tables = getNextBatchOfTables(globalState, depth, generateCTEs);
        }
        if (tables != null) {
            gen.setColumns(tables.getColumns());
        }

        MonetSelect select = new MonetSelect();
        List<MonetExpression> columns = new ArrayList<>(1);
        columns.add(gen.generateExpression(depth, tp));
        return createSelect(gen, globalState, tables, depth, select, columns, generateOrderBy, generateLimit);
    }

    private static MonetQuery createSingleColumnSet(MonetExpressionGenerator gen, MonetGlobalState globalState, int depth, MonetDataType tp) {
        MonetQuery left = createSingleColumnSelect(gen, globalState, null, depth, tp, false, false, false);
        MonetQuery right = createSingleColumnSelect(gen, globalState, null, depth, tp, false, false, false);
        MonetQuery res = new MonetSet(Randomly.fromList(Arrays.asList(SetType.values())), Randomly.fromList(Arrays.asList(SetDistictOrAll.values())), left, right);
        res.setFetchColumns(left.getFetchColumns());
        return res;
    }

    private static MonetQuery createSingleColumnValues(MonetExpressionGenerator gen, MonetGlobalState globalState, int depth, MonetDataType tp) {
        MonetTables tables = getNextBatchOfTables(globalState, depth, false);
        gen.setColumns(tables.getColumns());

        int nrRows = Randomly.smallNumber() + 1;
        List<List<MonetExpression>> rows = new ArrayList<>(nrRows);
        for (int i = 0 ; i < nrRows; i++) {
            List<MonetExpression> columns = new ArrayList<>(1);

            columns.add(gen.generateExpression(depth, tp));
            rows.add(columns);
        }
        MonetQuery res = new MonetValues(rows);
        res.setFetchColumns(rows.get(0));
        return res;
    }

    private static MonetQuery createRandomSingleColumnQueryInternal(MonetExpressionGenerator gen, MonetGlobalState globalState, int depth, MonetDataType tp, boolean generateOrderBy, boolean generateLimit) {
        switch (Randomly.fromOptions(1, 2, 3, 4)) {
            case 1:
            case 2:
                return createSingleColumnSelect(gen, globalState, null, depth, tp, generateOrderBy, generateLimit, true);
            case 3:
                return createSingleColumnSet(gen, globalState, depth, tp);
            case 4:
                return createSingleColumnValues(gen, globalState, depth, tp);
            default:
                throw new AssertionError();
        }
    }

    public static MonetQuery createRandomSingleColumnQuery(int depth, MonetDataType tp, MonetGlobalState globalState, boolean generateOrderBy, boolean generateLimit, boolean allowParameters) {
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState);
        gen.setAllowParameters(allowParameters);
        return createRandomSingleColumnQueryInternal(gen, globalState, depth, tp, generateOrderBy, generateLimit);
    }

}
