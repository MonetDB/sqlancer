package sqlancer.monet.oracle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Main.StateLogger;
import sqlancer.MainOptions;
import sqlancer.Randomly;
import sqlancer.StateToReproduce.MonetStateToReproduce;
import sqlancer.TestOracle;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetRowValue;
import sqlancer.monet.MonetSchema.MonetTables;
import sqlancer.monet.MonetToStringVisitor;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.gen.MonetExpressionGenerator;

public class MonetPivotedQuerySynthesisOracle implements TestOracle {

    private MonetStateToReproduce state;
    private MonetRowValue rw;
    private final Connection database;
    private List<MonetColumn> fetchColumns;
    private final MonetSchema s;
    private final MainOptions options;
    private final StateLogger logger;
    private final MonetGlobalState globalState;

    public MonetPivotedQuerySynthesisOracle(MonetGlobalState globalState) throws SQLException {
        this.globalState = globalState;
        this.database = globalState.getConnection();
        this.s = globalState.getSchema();
        options = globalState.getOptions();
        logger = globalState.getLogger();
    }

    @Override
    public void check() throws SQLException {
        // clear left-over query string from previous test
        state.queryString = null;
        String queryString = getQueryThatContainsAtLeastOneRow(state);
        state.queryString = queryString;
        if (options.logEachSelect()) {
            logger.writeCurrent(state.queryString);
        }

        boolean isContainedIn = isContainedIn(queryString, options, logger);
        if (!isContainedIn) {
            String assertionMessage = String.format("the query doesn't contain at least 1 row!\n-- %s;", queryString);
            throw new AssertionError(assertionMessage);
        }

    }

    public String getQueryThatContainsAtLeastOneRow(MonetStateToReproduce state) throws SQLException {
        this.state = state;
        MonetTables randomFromTables = s.getRandomTableNonEmptyTables();

        state.queryTargetedTablesString = randomFromTables.tableNamesAsString();

        MonetSelect selectStatement = new MonetSelect();
        selectStatement.setSelectType(Randomly.fromOptions(MonetSelect.SelectType.values()));
        List<MonetColumn> columns = randomFromTables.getColumns();
        rw = randomFromTables.getRandomRowValue(database, state);

        fetchColumns = columns;
        selectStatement.setFromList(randomFromTables.getTables().stream().map(t -> new MonetFromTable(t, false))
                .collect(Collectors.toList()));
        selectStatement.setFetchColumns(fetchColumns.stream()
                .map(c -> new MonetColumnValue(c, rw.getValues().get(c))).collect(Collectors.toList()));
        state.queryTargetedColumnsString = fetchColumns.stream().map(c -> c.getFullQualifiedName())
                .collect(Collectors.joining(", "));
        MonetExpression whereClause = generateWhereClauseThatContainsRowValue(columns, rw);
        selectStatement.setWhereClause(whereClause);
        state.whereClause = selectStatement;
        List<MonetExpression> groupByClause = generateGroupByClause(columns, rw);
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
            if (rw.getValues().get(c).isNull()) {
                sb2.append(" IS NULL");
            } else {
                sb2.append(" = ");
                sb2.append(rw.getValues().get(c).getTextRepresentation());
            }
        }
        sb2.append(") as result;");
        state.queryThatSelectsRow = sb2.toString();

        MonetToStringVisitor visitor = new MonetToStringVisitor();
        visitor.visit(selectStatement);
        return visitor.get();
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

    private boolean isContainedIn(String queryString, MainOptions options, StateLogger logger) throws SQLException {
        Statement createStatement;
        createStatement = database.createStatement();

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ("); // ANOTHER SELECT TO USE ORDER BY without restrictions
        sb.append(queryString);
        sb.append(") as result WHERE ");
        int i = 0;
        for (MonetColumn c : fetchColumns) {
            if (i++ != 0) {
                sb.append(" AND ");
            }
            sb.append("result." + c.getTable().getName() + c.getName());
            if (rw.getValues().get(c).isNull()) {
                sb.append(" IS NULL");
            } else {
                sb.append(" = ");
                sb.append(rw.getValues().get(c).getTextRepresentation());
            }
        }
        String resultingQueryString = sb.toString();
        // log both SELECT queries at the bottom of the error log file
        state.queryString = String.format("-- %s;\n-- %s;", queryString, resultingQueryString);
        if (options.logEachSelect()) {
            logger.writeCurrent(resultingQueryString);
        }
        try (ResultSet result = createStatement.executeQuery(resultingQueryString)) {
            boolean isContainedIn = result.next();
            createStatement.close();
            return isContainedIn;
        } catch (SQLException e) {
            if (e.getMessage().contains("out of range") || e.getMessage().contains("cannot cast")
                    || e.getMessage().contains("invalid input syntax for ") || e.getMessage().contains("conversion of ") || e.getMessage().contains("must be type")
                    || e.getMessage().contains("operator does not exist")
                    || e.getMessage().contains("division by zero")
                    || e.getMessage().contains("canceling statement due to statement timeout")) {
                return true;
            } else {
                throw e;
            }
        }
    }

}
