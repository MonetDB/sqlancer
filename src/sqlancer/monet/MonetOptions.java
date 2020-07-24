package sqlancer.monet;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.beust.jcommander.Parameter;
import com.beust.jcommander.Parameters;

import sqlancer.CompositeTestOracle;
import sqlancer.TestOracle;
import sqlancer.monet.oracle.MonetNoRECOracle;
import sqlancer.monet.oracle.MonetPivotedQuerySynthesisOracle;
import sqlancer.monet.oracle.tlp.MonetTLPAggregateOracle;
import sqlancer.monet.oracle.tlp.MonetTLPHavingOracle;
import sqlancer.monet.oracle.tlp.MonetTLPWhereOracle;

@Parameters
public class MonetOptions {

    @Parameter(names = "--bulk-insert")
    public boolean allowBulkInsert;

    @Parameter(names = "--oracle")
    public List<MonetOracle> oracle = Arrays.asList(MonetOracle.QUERY_PARTITIONING);

    public enum MonetOracle {
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
                return new CompositeTestOracle(oracles);
            }
        };

        public abstract TestOracle create(MonetGlobalState globalState) throws SQLException;

    }

}
