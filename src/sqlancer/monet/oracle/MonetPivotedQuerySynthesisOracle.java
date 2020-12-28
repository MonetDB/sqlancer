package sqlancer.monet.oracle;

import java.sql.SQLException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.SQLConnection;
import sqlancer.common.oracle.PivotedQuerySynthesisBase;
import sqlancer.common.query.Query;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetRowValue;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetPostfixOperation;
import sqlancer.monet.ast.MonetPostfixOperation.PostfixOperator;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.gen.MonetCommon;
import sqlancer.monet.gen.MonetExpressionGenerator;

public class MonetPivotedQuerySynthesisOracle
        extends PivotedQuerySynthesisBase<MonetGlobalState, MonetRowValue, MonetExpression, SQLConnection> {

    private List<MonetColumn> fetchColumns;

    public MonetPivotedQuerySynthesisOracle(MonetGlobalState globalState) throws SQLException {
        super(globalState);
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonFetchErrors(errors);
    }

    @Override
    public SQLQueryAdapter getRectifiedQuery() throws SQLException {
        MonetTables randomFromTables = globalState.getSchema().getRandomTableNonEmptyTables();

        MonetSelect selectStatement = new MonetSelect();
        selectStatement.setSelectType(Randomly.fromOptions(MonetSelect.SelectType.values()));
        List<MonetColumn> columns = randomFromTables.getColumns();
        pivotRow = randomFromTables.getRandomRowValue(globalState.getConnection());

        fetchColumns = columns;
        selectStatement.setFromList(randomFromTables.getTables().stream().map(t -> new MonetFromTable(t, false, null))
                .collect(Collectors.toList()));
        selectStatement.setFetchColumns(fetchColumns.stream()
                .map(c -> new MonetColumnValue(getFetchValueAliasedColumn(c), pivotRow.getValues().get(c)))
                .collect(Collectors.toList()));
        MonetExpression whereClause = generateRectifiedExpression(columns, pivotRow);
        selectStatement.setWhereClause(whereClause);
        List<MonetExpression> groupByClause = generateGroupByClause(columns, pivotRow);
        selectStatement.setGroupByExpressions(groupByClause);
        MonetExpression limitClause = generateLimit();
        selectStatement.setLimitClause(limitClause);
        if (limitClause != null) {
            MonetExpression offsetClause = generateOffset();
            selectStatement.setOffsetClause(offsetClause);
        }
        List<MonetExpression> orderBy = new MonetExpressionGenerator(globalState).setColumns(columns).generateOrderBy();
        selectStatement.setOrderByExpressions(orderBy);

        return new SQLQueryAdapter(MonetVisitor.asString(selectStatement));
    }

    /*
     * Prevent name collisions by aliasing the column.
     */
    private MonetColumn getFetchValueAliasedColumn(MonetColumn c) {
        MonetColumn aliasedColumn = new MonetColumn(c.getName() + " AS " + c.getTable().getName() + c.getName(),
                c.getType());
        aliasedColumn.setTable(c.getTable());
        return aliasedColumn;
    }

    private List<MonetExpression> generateGroupByClause(List<MonetColumn> columns, MonetRowValue rw) {
        if (Randomly.getBoolean()) {
            return columns.stream().map(c -> MonetColumnValue.create(c, rw.getValues().get(c)))
                    .collect(Collectors.toList());
        } else {
            return Collections.emptyList();
        }
    }

    private MonetConstant generateLimit() {
        if (Randomly.getBoolean()) {
            return MonetConstant.createIntConstant(Integer.MAX_VALUE, MonetDataType.INT);
        } else {
            return null;
        }
    }

    private MonetExpression generateOffset() {
        if (Randomly.getBoolean()) {
            return MonetConstant.createIntConstant(0, MonetDataType.INT);
        } else {
            return null;
        }
    }

    private MonetExpression generateRectifiedExpression(List<MonetColumn> columns, MonetRowValue rw) {
        MonetExpression expr = new MonetExpressionGenerator(globalState).setColumns(columns).setRowValue(rw)
                .generateExpressionWithExpectedResult(MonetDataType.BOOLEAN);
        MonetExpression result;
        if (expr.getExpectedValue().isNull()) {
            result = MonetPostfixOperation.create(expr, PostfixOperator.IS_NULL);
        } else {
            result = MonetPostfixOperation.create(expr, expr.getExpectedValue().cast(MonetDataType.BOOLEAN).asBoolean()
                    ? PostfixOperator.IS_TRUE : PostfixOperator.IS_FALSE);
        }
        rectifiedPredicates.add(result);
        return result;
    }

    @Override
    protected Query<SQLConnection> getContainmentCheckQuery(Query<?> query) throws SQLException {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ("); // ANOTHER SELECT TO USE ORDER BY without restrictions
        sb.append(query.getUnterminatedQueryString());
        sb.append(") as result WHERE ");
        int i = 0;
        for (MonetColumn c : fetchColumns) {
            if (i++ != 0) {
                sb.append(" AND ");
            }
            sb.append("result.");
            sb.append(c.getTable().getName());
            sb.append(c.getName());
            if (pivotRow.getValues().get(c).isNull()) {
                sb.append(" IS NULL");
            } else {
                sb.append(" = ");
                sb.append(pivotRow.getValues().get(c).getTextRepresentation());
            }
        }
        String resultingQueryString = sb.toString();
        return new SQLQueryAdapter(resultingQueryString, errors);
    }

    @Override
    protected String getExpectedValues(MonetExpression expr) {
        return MonetVisitor.asExpectedValues(expr);
    }

}
