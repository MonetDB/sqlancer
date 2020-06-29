package sqlancer.monet.gen;

import java.util.Arrays;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;

public final class MonetVacuumGenerator {

    private MonetVacuumGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        StringBuilder sb = new StringBuilder("CALL sys.");
        sb.append(Randomly.fromOptions("shrink", "reuse", "vacuum"));
        sb.append("('sys', '");
        sb.append(globalState.getSchema().getRandomTable().getName());
        sb.append("')");

        return new QueryAdapter(sb.toString(), Arrays.asList("vacuum not allowed on tables with indices", "reuse not allowed on tables with indices", "shrink not allowed on tables with indices"));
    }

}
