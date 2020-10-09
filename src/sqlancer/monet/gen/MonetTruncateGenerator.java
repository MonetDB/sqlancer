package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;

public final class MonetTruncateGenerator {

    private MonetTruncateGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        StringBuilder sb = new StringBuilder();
        sb.append("TRUNCATE");
        if (Randomly.getBoolean()) {
            sb.append(" TABLE");
        }
        sb.append(" ");
        sb.append(globalState.getSchema().getRandomTable(t -> t.isInsertable()).getName());
        if (Randomly.getBoolean()) {
            sb.append(" ");
            sb.append(Randomly.fromOptions("RESTART IDENTITY", "CONTINUE IDENTITY"));
        }
        if (Randomly.getBoolean()) {
            sb.append(" ");
            sb.append(Randomly.fromOptions("CASCADE", "RESTRICT"));
        }
        ExpectedErrors errors = ExpectedErrors.from("cannot truncate a table referenced in a foreign key constraint", "is not a table", "cannot truncate view", "FOREIGN KEY");
        MonetCommon.addCommonInsertUpdateErrors(errors);
        return new QueryAdapter(sb.toString(), errors);
    }

}
