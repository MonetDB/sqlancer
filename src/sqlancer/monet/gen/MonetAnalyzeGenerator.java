package sqlancer.monet.gen;

import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
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
        return new QueryAdapter(sb.toString(), ExpectedErrors.from("does not exist", "is not persistent"));
    }

}
