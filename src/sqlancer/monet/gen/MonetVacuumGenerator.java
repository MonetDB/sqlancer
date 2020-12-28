package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;

public final class MonetVacuumGenerator {

    private MonetVacuumGenerator() {
    }

    public static SQLQueryAdapter create(MonetGlobalState globalState) {
        StringBuilder sb = new StringBuilder("CALL sys.");
        sb.append(Randomly.fromOptions("shrink", "reuse", "vacuum"));
        sb.append("('sys', '");
        sb.append(globalState.getSchema().getRandomTable(t -> t.isInsertable()).getName());
        sb.append("')");

        return new SQLQueryAdapter(sb.toString(), ExpectedErrors.from("not allowed on tables with indices", "is not persistent"));
    }

}
