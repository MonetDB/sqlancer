package sqlancer.monet;

import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.AbstractAction;
import sqlancer.CompositeTestOracle;
import sqlancer.IgnoreMeException;
import sqlancer.ProviderAdapter;
import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.QueryProvider;
import sqlancer.Randomly;
import sqlancer.StateToReproduce;
import sqlancer.StateToReproduce.MonetStateToReproduce;
import sqlancer.StatementExecutor;
import sqlancer.TestOracle;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.gen.MonetAlterTableGenerator;
import sqlancer.monet.gen.MonetAnalyzeGenerator;
import sqlancer.monet.gen.MonetCommentGenerator;
import sqlancer.monet.gen.MonetDeleteGenerator;
import sqlancer.monet.gen.MonetDropIndexGenerator;
import sqlancer.monet.gen.MonetIndexGenerator;
import sqlancer.monet.gen.MonetInsertGenerator;
import sqlancer.monet.gen.MonetQueryCatalogGenerator;
import sqlancer.monet.gen.MonetTableGenerator;
import sqlancer.monet.gen.MonetTransactionGenerator;
import sqlancer.monet.gen.MonetTruncateGenerator;
import sqlancer.monet.gen.MonetUpdateGenerator;
import sqlancer.monet.gen.MonetVacuumGenerator;
import sqlancer.monet.gen.MonetViewGenerator;
import sqlancer.sqlite3.gen.SQLite3Common;

public final class MonetProvider extends ProviderAdapter<MonetGlobalState, MonetOptions> {

    public static boolean generateOnlyKnown;

    //private MonetGlobalState globalState;

    public MonetProvider() {
        super(MonetGlobalState.class, MonetOptions.class);
    }

    public enum Action implements AbstractAction<MonetGlobalState> {
        ANALYZE(MonetAnalyzeGenerator::create), //
        ALTER_TABLE(g -> MonetAlterTableGenerator.create(g.getSchema().getRandomTable(t -> !t.isView()), g,
                generateOnlyKnown)), //
        COMMIT(g -> {
            Query query;
            if (Randomly.getBoolean()) {
                query = new QueryAdapter("COMMIT", Arrays.asList("not allowed in auto commit mode"), true);
            } else if (Randomly.getBoolean()) {
                query = MonetTransactionGenerator.executeBegin();
            } else {
                query = new QueryAdapter("ROLLBACK", true);
            }
            return query;
        }), //
        DELETE(MonetDeleteGenerator::create), //
        DROP_INDEX(MonetDropIndexGenerator::create), //
        INSERT(MonetInsertGenerator::insert), //
        UPDATE(MonetUpdateGenerator::create), //
        TRUNCATE(MonetTruncateGenerator::create), //
        VACUUM(MonetVacuumGenerator::create), //
        //SET(MonetSetGenerator::create), //
        CREATE_INDEX(MonetIndexGenerator::generate), //
        /*SET_CONSTRAINTS((g) -> {
            StringBuilder sb = new StringBuilder();
            sb.append("SET CONSTRAINTS ALL ");
            sb.append(Randomly.fromOptions("DEFERRED", "IMMEDIATE"));
            return new QueryAdapter(sb.toString());
        }), */
        COMMENT_ON(MonetCommentGenerator::generate), //
        //CREATE_SEQUENCE(MonetSequenceGenerator::createSequence), //
        CREATE_VIEW(MonetViewGenerator::create), //
        QUERY_CATALOG((g) -> MonetQueryCatalogGenerator.query());

        private final QueryProvider<MonetGlobalState> queryProvider;

        Action(QueryProvider<MonetGlobalState> queryProvider) {
            this.queryProvider = queryProvider;
        }

        @Override
        public Query getQuery(MonetGlobalState state) throws SQLException {
            return queryProvider.getQuery(state);
        }
    }

    private static int mapActions(MonetGlobalState globalState, Action a) {
        Randomly r = globalState.getRandomly();
        int nrPerformed;
        switch (a) {
        case VACUUM:
        case CREATE_INDEX:
        case DROP_INDEX:
            nrPerformed = r.getInteger(0, 1);
            break;
        case COMMIT:
            nrPerformed = r.getInteger(0, 0);
            break;
        case ALTER_TABLE:
            nrPerformed = r.getInteger(0, 3);
            break;
        case DELETE:
        //case SET:
        case QUERY_CATALOG:
            nrPerformed = r.getInteger(0, 3);
            break;
        case ANALYZE:
            nrPerformed = r.getInteger(0, 3);
            break;
        //case SET_CONSTRAINTS:
        case COMMENT_ON:
        //case CREATE_SEQUENCE:
        case TRUNCATE:
            nrPerformed = r.getInteger(0, 2);
            break;
        case CREATE_VIEW:
            nrPerformed = r.getInteger(0, 2);
            break;
        case UPDATE:
            nrPerformed = r.getInteger(0, 10);
            break;
        case INSERT:
            nrPerformed = r.getInteger(0, globalState.getOptions().getMaxNumberInserts());
            break;
        default:
            throw new AssertionError(a);
        }
        return nrPerformed;

    }

    @Override
    public void generateDatabase(MonetGlobalState globalState) throws SQLException {
        while (globalState.getSchema().getDatabaseTables().size() < Randomly.fromOptions(1, 2)) {
            try {
                String tableName = SQLite3Common.createTableName(globalState.getSchema().getDatabaseTables().size());
                Query createTable = MonetTableGenerator.generate(tableName, globalState.getSchema(),
                        generateOnlyKnown, globalState);
                globalState.executeStatement(createTable);
            } catch (IgnoreMeException e) {

            }
        }

        StatementExecutor<MonetGlobalState, Action> se = new StatementExecutor<>(globalState, Action.values(),
                MonetProvider::mapActions, (q) -> {
                    if (globalState.getSchema().getDatabaseTables().isEmpty()) {
                        throw new IgnoreMeException();
                    }
                });
        se.executeStatements();
        globalState.executeStatement(new QueryAdapter("COMMIT", true));
        globalState.executeStatement(new QueryAdapter("CALL sys.setquerytimeout(5000);\n"));
    }

    @Override
    protected TestOracle getTestOracle(MonetGlobalState globalState) throws SQLException {
        List<TestOracle> oracles = globalState.getDmbsSpecificOptions().oracle.stream().map(o -> {
            try {
                return o.create(globalState);
            } catch (SQLException e1) {
                throw new AssertionError(e1);
            }
        }).collect(Collectors.toList());
        return new CompositeTestOracle(oracles);
    }

    @Override
    public Connection createDatabase(MonetGlobalState globalState) throws SQLException {
        String url = "jdbc:monetdb://localhost:50000/:inmemory";
        Connection con = DriverManager.getConnection(url, "monetdb", "monetdb");

        //TODO clean database
        return con;
    }

    @Override
    public String getDBMSName() {
        return "monetdb";
    }

    @Override
    public void printDatabaseSpecificState(FileWriter writer, StateToReproduce state) {
        StringBuilder sb = new StringBuilder();
        MonetStateToReproduce specificState = (MonetStateToReproduce) state;
        if (specificState.getRandomRowValues() != null) {
            List<MonetColumn> columnList = specificState.getRandomRowValues().keySet().stream()
                    .collect(Collectors.toList());
            List<MonetTable> tableList = columnList.stream().map(c -> c.getTable()).distinct().sorted()
                    .collect(Collectors.toList());
            for (MonetTable t : tableList) {
                sb.append("-- " + t.getName() + "\n");
                List<MonetColumn> columnsForTable = columnList.stream().filter(c -> c.getTable().equals(t))
                        .collect(Collectors.toList());
                for (MonetColumn c : columnsForTable) {
                    sb.append("--\t");
                    sb.append(c);
                    sb.append("=");
                    sb.append(specificState.getRandomRowValues().get(c));
                    sb.append("\n");
                }
            }
            sb.append("expected values: \n");
            MonetExpression whereClause = ((MonetStateToReproduce) state).getWhereClause();
            if (whereClause != null) {
                sb.append(MonetVisitor.asExpectedValues(whereClause).replace("\n", "\n-- "));
            }
        }
        try {
            writer.write(sb.toString());
            writer.flush();
        } catch (IOException e) {
            throw new AssertionError();
        }
    }

    @Override
    public StateToReproduce getStateToReproduce(String databaseName) {
        return new MonetStateToReproduce(databaseName);
    }
}
