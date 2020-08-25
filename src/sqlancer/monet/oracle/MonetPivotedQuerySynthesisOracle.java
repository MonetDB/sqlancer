package sqlancer.monet.oracle;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Main.StateLogger;
import sqlancer.MainOptions;
import sqlancer.Randomly;
import sqlancer.common.oracle.PivotedQuerySynthesisBase;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetRowValue;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.MonetToStringVisitor;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.gen.MonetCommon;
import sqlancer.monet.gen.MonetExpressionGenerator;

public class MonetPivotedQuerySynthesisOracle
        extends PivotedQuerySynthesisBase<MonetGlobalState, MonetRowValue, MonetExpression> {

    private List<MonetColumn> fetchColumns;
    private final MainOptions options;
    private final StateLogger logger;

    public MonetPivotedQuerySynthesisOracle(MonetGlobalState globalState) throws SQLException {
        super(globalState);
        options = globalState.getOptions();
        logger = globalState.getLogger();
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonFetchErrors(errors);
    }

    @Override
    public Query getQueryThatContainsAtLeastOneRow() throws SQLException {
        MonetTables randomFromTables = globalState.getSchema().getRandomTableNonEmptyTables();

        MonetSelect selectStatement = new MonetSelect();
        selectStatement.setSelectType(Randomly.fromOptions(MonetSelect.SelectType.values()));
        List<MonetColumn> columns = randomFromTables.getColumns();
        pivotRow = randomFromTables.getRandomRowValue(globalState.getConnection());

        fetchColumns = columns;
        selectStatement.setFromList(randomFromTables.getTables().stream().map(t -> new MonetFromTable(t, false))
                .collect(Collectors.toList()));
        selectStatement.setFetchColumns(fetchColumns.stream()
                .map(c -> new MonetColumnValue(getFetchValueAliasedColumn(c), pivotRow.getValues().get(c)))
                .collect(Collectors.toList()));
        MonetExpression whereClause = generateWhereClauseThatContainsRowValue(columns, pivotRow);
        selectStatement.setWhereClause(whereClause);
        List<MonetExpression> groupByClause = generateGroupByClause(columns, pivotRow);
        selectStatement.setGroupByExpressions(groupByClause);
        MonetExpression limitClause = generateLimit();
        selectStatement.setLimitClause(limitClause);
        if (limitClause != null) {
            MonetExpression offsetClause = generateOffset();
            selectStatement.setOffsetClause(offsetClause);
        }
        List<MonetExpression> orderBy = new MonetExpressionGenerator(globalState).setColumns(columns)
                .generateOrderBy();
        selectStatement.setOrderByExpressions(orderBy);

        StringBuilder sb2 = new StringBuilder();
        sb2.append("SELECT * FROM (SELECT 1 FROM ");
        sb2.append(randomFromTables.tableNamesAsString());
        sb2.append(" WHERE ");
        int i = 0;
        for (MonetColumn c : fetchColumns) {
            if (i++ != 0) {
                sb2.append(" AND ");
            }
            sb2.append(c.getFullQualifiedName());
            if (pivotRow.getValues().get(c).isNull()) {
                sb2.append(" IS NULL");
            } else {
                sb2.append(" = ");
                sb2.append(pivotRow.getValues().get(c).getTextRepresentation());
            }
        }
        sb2.append(") as result(");
        i = 0;
        for (MonetColumn c : fetchColumns) {
            if (i++ != 0) {
                sb2.append(",");
            }
            sb2.append(c.getFullQualifiedName());
        }
        sb2.append(");");

        MonetToStringVisitor visitor = new MonetToStringVisitor();
        visitor.visit(selectStatement);
        return new QueryAdapter(visitor.get());
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
            return MonetConstant.createIntConstant(Integer.MAX_VALUE);
        } else {
            return null;
        }
    }

    private MonetExpression generateOffset() {
        if (Randomly.getBoolean()) {
            // OFFSET 0
            return MonetConstant.createIntConstant(0);
        } else {
            return null;
        }
    }

    private MonetExpression generateWhereClauseThatContainsRowValue(List<MonetColumn> columns,
            MonetRowValue rw) {
        return MonetExpressionGenerator.generateTrueCondition(columns, rw, globalState);
    }

    @Override
    protected boolean isContainedIn(Query query) throws SQLException {
        Statement createStatement;
        createStatement = globalState.getConnection().createStatement();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ("); // ANOTHER SELECT TO USE ORDER BY without restrictions
        if (query.getQueryString().endsWith(";")) {
            sb.append(query.getQueryString().substring(0, query.getQueryString().length() - 1));
        } else {
            sb.append(query.getQueryString());
        }
        sb.append(") as result(");
        int i = 0;
        for (MonetColumn c : fetchColumns) {
            if (i++ != 0) {
                sb.append(",");
            }
            sb.append(c.getTable().getName());
            sb.append(c.getName());
        }
        sb.append(") WHERE ");
        i = 0;
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
        // log both SELECT queries at the bottom of the error log file
        if (options.logEachSelect()) {
            logger.writeCurrent(resultingQueryString);
        }
        globalState.getState().getLocalState().log(resultingQueryString);
        QueryAdapter finalQuery = new QueryAdapter(resultingQueryString, errors);
        try (ResultSet result = createStatement.executeQuery(resultingQueryString)) {
            boolean isContainedIn = result.next();
            createStatement.close();
            return isContainedIn;
        } catch (SQLException e) {
            if (finalQuery.getExpectedErrors().errorIsExpected(e.getMessage())) {
                return true;
            } else {
                throw e;
            }
        }
    }

    @Override
    protected String asString(MonetExpression expr) {
        return MonetVisitor.asString(expr);
    }

}
