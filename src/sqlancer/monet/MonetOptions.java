package sqlancer.monet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import sqlancer.DBMSSpecificOptions;
import sqlancer.OracleFactory;
import sqlancer.common.oracle.CompositeTestOracle;
import sqlancer.common.oracle.TestOracle;
import sqlancer.monet.MonetOptions.MonetOracleFactory;
import sqlancer.monet.oracle.MonetNoRECOracle;
import sqlancer.monet.oracle.MonetPivotedQuerySynthesisOracle;
import sqlancer.monet.oracle.tlp.MonetTLPAggregateOracle;
import sqlancer.monet.oracle.tlp.MonetTLPHavingOracle;
import sqlancer.monet.oracle.tlp.MonetTLPWhereOracle;

@Parameters
public class MonetOptions implements DBMSSpecificOptions<MonetOracleFactory> {

    @Parameter(names = "--bulk-insert", description = "Specifies whether INSERT statements should be issued in bulk", arity = 1)
    public boolean allowBulkInsert = true;

    @Parameter(names = "--oracle", description = "Specifies which test oracle should be used for MonetQL")
    public List<MonetOracleFactory> oracle = Arrays.asList(MonetOracleFactory.QUERY_PARTITIONING);

    @Parameter(names = "--connection-url", description = "Specifies the URL for connecting to the MonetQL server", arity = 1)
    public String connectionURL = "mapi://localhost:5432/test";

    public enum MonetOracleFactory implements OracleFactory<MonetGlobalState> {
        NOREC {
            @Override
            public TestOracle create(MonetGlobalState globalState) throws SQLException {
                return new MonetNoRECOracle(globalState);
            }
        },
        PQS {
            @Override
            public TestOracle create(MonetGlobalState globalState) throws SQLException {
                return new MonetPivotedQuerySynthesisOracle(globalState);
            }

            @Override
            public boolean requiresAllTablesToContainRows() {
                return true;
            }
        },
        HAVING {

            @Override
            public TestOracle create(MonetGlobalState globalState) throws SQLException {
                return new MonetTLPHavingOracle(globalState);
            }

        },
        QUERY_PARTITIONING {
            @Override
            public TestOracle create(MonetGlobalState globalState) throws SQLException {
                List<TestOracle> oracles = new ArrayList<>();
                oracles.add(new MonetTLPWhereOracle(globalState));
                oracles.add(new MonetTLPHavingOracle(globalState));
                oracles.add(new MonetTLPAggregateOracle(globalState));
                return new CompositeTestOracle(oracles, globalState);
            }
        };

    }

    @Override
    public List<MonetOracleFactory> getTestOracleFactory() {
        return oracle;
    }

}
