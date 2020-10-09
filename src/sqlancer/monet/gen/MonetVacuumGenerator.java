package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;

public final class MonetVacuumGenerator {

    private MonetVacuumGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        StringBuilder sb = new StringBuilder("CALL sys.");
        sb.append(Randomly.fromOptions("shrink", "reuse", "vacuum"));
        sb.append("('sys', '");
        sb.append(globalState.getSchema().getRandomTable(t -> t.isInsertable()));
        sb.append("')");

        return new QueryAdapter(sb.toString(), ExpectedErrors.from("not allowed on tables with indices", "is not persistent"));
    }

}
