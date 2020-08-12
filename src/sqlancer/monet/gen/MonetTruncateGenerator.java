package sqlancer.monet.gen;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
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
        sb.append(globalState.getSchema().getRandomTable().getName());
        if (Randomly.getBoolean()) {
            sb.append(" ");
            sb.append(Randomly.fromOptions("RESTART IDENTITY", "CONTINUE IDENTITY"));
        }
        if (Randomly.getBoolean()) {
            sb.append(" ");
            sb.append(Randomly.fromOptions("CASCADE", "RESTRICT"));
        }
        Set<String> errors = new HashSet<>(Arrays.asList("cannot truncate a table referenced in a foreign key constraint", "is not a table", "cannot truncate view"));
        MonetCommon.addCommonInsertUpdateErrors(errors);
        return new QueryAdapter(sb.toString(), errors);
    }

}
