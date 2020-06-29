package sqlancer.monet.gen;

import java.util.Arrays;
import java.util.List;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetIndex;

public final class MonetDropIndexGenerator {

    private MonetDropIndexGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        List<MonetIndex> indexes = globalState.getSchema().getRandomTable().getIndexes();
        StringBuilder sb = new StringBuilder();
        sb.append("DROP INDEX ");
        if (indexes.isEmpty()) {
            sb.append("iamdummy");
        } else {
            sb.append(Randomly.fromList(indexes).getIndexName());
        }
        return new QueryAdapter(sb.toString(), Arrays.asList("no such index"), true);
    }

}
