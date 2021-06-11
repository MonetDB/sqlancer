package sqlancer.monet;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import sqlancer.AbstractAction;
import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.SQLConnection;
import sqlancer.SQLProviderAdapter;
import sqlancer.StatementExecutor;
import sqlancer.common.DBMSCommon;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.common.query.SQLQueryProvider;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.gen.MonetAlterTableGenerator;
import sqlancer.monet.gen.MonetAnalyzeGenerator;
import sqlancer.monet.gen.MonetCommentGenerator;
import sqlancer.monet.gen.MonetDeleteGenerator;
//import sqlancer.monet.gen.MonetDropIndexGenerator;
//import sqlancer.monet.gen.MonetIndexGenerator;
import sqlancer.monet.gen.MonetInsertGenerator;
import sqlancer.monet.gen.MonetLoggerSuspenderGenerator;
import sqlancer.monet.gen.MonetMergeGenerator;
import sqlancer.monet.gen.MonetPreparedStatementGenerator;
import sqlancer.monet.gen.MonetQueryCatalogGenerator;
import sqlancer.monet.gen.MonetTableGenerator;
import sqlancer.monet.gen.MonetTransactionGenerator;
import sqlancer.monet.gen.MonetTruncateGenerator;
import sqlancer.monet.gen.MonetUpdateGenerator;
import sqlancer.monet.gen.MonetViewGenerator;

public class MonetProvider extends SQLProviderAdapter<MonetGlobalState, MonetOptions> {

    /**
     * Generate only data types and expressions that are understood by PQS.
     */
    public static final boolean GENERATE_ONLY_KNOWN = false;

    public MonetProvider() {
        super(MonetGlobalState.class, MonetOptions.class);
    }

    protected MonetProvider(Class<MonetGlobalState> globalClass, Class<MonetOptions> optionClass) {
        super(globalClass, optionClass);
    }

    public enum Action implements AbstractAction<MonetGlobalState> {
        ANALYZE(MonetAnalyzeGenerator::create), //
        ALTER_TABLE(g -> MonetAlterTableGenerator.create(g.getSchema().getRandomTable(t -> !t.isView()), g, false)), //
        COMMIT(g -> {
            SQLQueryAdapter query;
            if (Randomly.getBoolean()) {
                query = new SQLQueryAdapter("COMMIT", ExpectedErrors.from("not allowed in auto commit mode"), true);
            } else if (Randomly.getBoolean()) {
                query = MonetTransactionGenerator.executeBegin();
            } else {
                query = new SQLQueryAdapter("ROLLBACK", true);
            }
            return query;
        }), //
        DELETE(MonetDeleteGenerator::create), //
        //DROP_INDEX(MonetDropIndexGenerator::create), //
        INSERT(MonetInsertGenerator::insert), //
        UPDATE(MonetUpdateGenerator::create), //
        TRUNCATE(MonetTruncateGenerator::create), //
        MERGE(MonetMergeGenerator::create), //
        LOGGER(MonetLoggerSuspenderGenerator::create), //
        //CREATE_INDEX(MonetIndexGenerator::generate), //
        COMMENT_ON(MonetCommentGenerator::generate), //
        //CREATE_SEQUENCE(MonetSequenceGenerator::createSequence), //
        CREATE_VIEW(MonetViewGenerator::create), //
        PREPARE(MonetPreparedStatementGenerator::create), //
        QUERY_CATALOG((g) -> MonetQueryCatalogGenerator.query());

        private final SQLQueryProvider<MonetGlobalState> queryProvider;

        Action(SQLQueryProvider<MonetGlobalState> queryProvider) {
            this.queryProvider = queryProvider;
        }

        @Override
        public SQLQueryAdapter getQuery(MonetGlobalState state) throws Exception {
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
        case COMMENT_ON:
        //case CREATE_SEQUENCE:
        //case DROP_INDEX:
            nrPerformed = r.getInteger(0, 1);
            break;
        case ANALYZE:
        case LOGGER:
        case QUERY_CATALOG:
        //case CREATE_INDEX:
        case TRUNCATE:
        case ALTER_TABLE:
            nrPerformed = r.getInteger(0, 2);
            break;
        case DELETE:
        case MERGE:
            nrPerformed = r.getInteger(0, 3);
            break;
        case CREATE_VIEW:
            nrPerformed = r.getInteger(0, 1);
            break;
        case UPDATE:
            nrPerformed = r.getInteger(0, 5);
            break;
        case PREPARE:
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
    public void generateDatabase(MonetGlobalState globalState) throws Exception {
        // readFunctions(globalState);
        createTables(globalState, 3); // Randomly.fromOptions(4, 5, 6));
        prepareTables(globalState);
    }

    @Override
    public SQLConnection createDatabase(MonetGlobalState globalState) throws SQLException {
        /*
         * if (globalState.getDmbsSpecificOptions().getTestOracleFactory().stream() .anyMatch((o) -> o ==
         * MonetOracleFactory.PQS)) { GENERATE_ONLY_KNOWN = true; }
         */

        MonetDataType.intitializeTypes();
        String url = "jdbc:monetdb://localhost:50000/";
        Connection con = DriverManager.getConnection(url, "monetdb", "monetdb");

        // TODO clean database
        return new SQLConnection(con);
    }

    protected void createTables(MonetGlobalState globalState, int numTables) throws Exception {
        while (globalState.getSchema().getDatabaseTables().size() < numTables) {
            try {
                String tableName = DBMSCommon.createTableName(globalState.getSchema().getDatabaseTables().size());
                SQLQueryAdapter createTable = MonetTableGenerator.generate(tableName, globalState.getSchema(),
                        GENERATE_ONLY_KNOWN, globalState);
                globalState.executeStatement(createTable);
            } catch (IgnoreMeException e) {

            }
        }
    }

    protected void prepareTables(MonetGlobalState globalState) throws Exception {
        StatementExecutor<MonetGlobalState, Action> se = new StatementExecutor<>(globalState, Action.values(),
                MonetProvider::mapActions, (q) -> {
                    if (globalState.getSchema().getDatabaseTables().isEmpty()) {
                        throw new IgnoreMeException();
                    }
                });
        se.executeStatements();
        globalState.executeStatement(new SQLQueryAdapter("CALL sys.setquerytimeout(5000);\n"));
    }

    @Override
    public String getDBMSName() {
        return "monetdb";
    }

}
