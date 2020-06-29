package sqlancer.monet.gen;

import java.util.Arrays;
import java.util.stream.Collectors;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetTable;

public final class MonetAnalyzeGenerator {

    private MonetAnalyzeGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        MonetTable table = globalState.getSchema().getRandomTable();
        StringBuilder sb = new StringBuilder("ANALYZE");
        sb.append(" sys.");
        sb.append(table.getName());
        if (Randomly.getBoolean()) {
            sb.append("(");
            sb.append(table.getColumns().stream().map(c -> c.getName())
                    .collect(Collectors.joining(", ")));
            sb.append(")");
        }
        if (Randomly.getBoolean()) {
            sb.append(" SAMPLE ").append(Randomly.getPositiveOrZeroNonCachedInteger());
        }
        if (Randomly.getBoolean()) {
            sb.append(" MINMAX");
        }
        return new QueryAdapter(sb.toString(), Arrays.asList("does not exist", "is not persistent"));
    }

}
