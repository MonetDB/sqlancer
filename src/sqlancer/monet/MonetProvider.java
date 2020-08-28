package sqlancer.monet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import sqlancer.AbstractAction;
import sqlancer.IgnoreMeException;
import sqlancer.ProviderAdapter;
import sqlancer.Randomly;
import sqlancer.StatementExecutor;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.common.query.QueryProvider;
import sqlancer.monet.MonetOptions.MonetOracleFactory;
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

public class MonetProvider extends ProviderAdapter<MonetGlobalState, MonetOptions> {

    /**
     * Generate only data types and expressions that are understood by PQS.
     */
    public static boolean generateOnlyKnown;

    //private MonetGlobalState globalState;

    public MonetProvider() {
        super(MonetGlobalState.class, MonetOptions.class);
    }

    protected MonetProvider(Class<MonetGlobalState> globalClass, Class<MonetOptions> optionClass) {
        super(globalClass, optionClass);
    }

    public enum Action implements AbstractAction<MonetGlobalState> {
        ANALYZE(MonetAnalyzeGenerator::create), //
        ALTER_TABLE(g -> MonetAlterTableGenerator.create(g.getSchema().getRandomTable(t -> !t.isView()), g,
                generateOnlyKnown)), //
        COMMIT(g -> {
            Query query;
            if (Randomly.getBoolean()) {
                query = new QueryAdapter("COMMIT", ExpectedErrors.from("not allowed in auto commit mode"), true);
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
        case COMMIT:
            nrPerformed = r.getInteger(0, 0);
            break;
        //case SET_CONSTRAINTS:
        case COMMENT_ON:
        //case CREATE_SEQUENCE:
        case TRUNCATE:
        case QUERY_CATALOG:
        case CREATE_VIEW:
        case CREATE_INDEX:
        case DROP_INDEX:
            nrPerformed = r.getInteger(0, 2);
            break;
        case ALTER_TABLE:
        case DELETE:
        case VACUUM:
        case ANALYZE:
            nrPerformed = r.getInteger(0, 3);
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
        //readFunctions(globalState);
        createTables(globalState, 3); //Randomly.fromOptions(4, 5, 6));
        prepareTables(globalState);
    }

    @Override
    public Connection createDatabase(MonetGlobalState globalState) throws SQLException {
        if (globalState.getDmbsSpecificOptions().getTestOracleFactory().stream()
                .anyMatch((o) -> o == MonetOracleFactory.PQS)) {
            generateOnlyKnown = true;
        }
        String url = "jdbc:monetdb://localhost:50000/:inmemory";
        Connection con = DriverManager.getConnection(url, "monetdb", "monetdb");

        //TODO clean database
        return con;
    }

    /*protected void readFunctions(MonetGlobalState globalState) throws SQLException {
        QueryAdapter query = new QueryAdapter("SELECT proname, provolatile FROM pg_proc;");
        SQLancerResultSet rs = query.executeAndGet(globalState);
        while (rs.next()) {
            String functionName = rs.getString(1);
            Character functionType = rs.getString(2).charAt(0);
            globalState.addFunctionAndType(functionName, functionType);
        }
    }*/

    protected void createTables(MonetGlobalState globalState, int numTables) throws SQLException {
        while (globalState.getSchema().getDatabaseTables().size() < numTables) {
            try {
                String tableName = SQLite3Common.createTableName(globalState.getSchema().getDatabaseTables().size());
                Query createTable = MonetTableGenerator.generate(tableName, globalState.getSchema(),
                        generateOnlyKnown, globalState);
                globalState.executeStatement(createTable);
            } catch (IgnoreMeException e) {

            }
        }
    }

    protected void prepareTables(MonetGlobalState globalState) throws SQLException {
        StatementExecutor<MonetGlobalState, Action> se = new StatementExecutor<>(globalState, Action.values(),
                MonetProvider::mapActions, (q) -> {
                    if (globalState.getSchema().getDatabaseTables().isEmpty()) {
                        throw new IgnoreMeException();
                    }
                });
        se.executeStatements();
        globalState.executeStatement(new QueryAdapter("CALL sys.setquerytimeout(5000);\n"));
    }

    @Override
    public String getDBMSName() {
        return "monetdb";
    }

}
