package sqlancer.monet.gen;

import java.util.List;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetIndex;

public final class MonetDropIndexGenerator {

    private MonetDropIndexGenerator() {
    }

    public static SQLQueryAdapter create(MonetGlobalState globalState) {
        List<MonetIndex> indexes = globalState.getSchema().getRandomTable().getIndexes();
        StringBuilder sb = new StringBuilder();
        sb.append("DROP INDEX ");
        if (indexes.isEmpty()) {
            sb.append("iamdummy");
        } else {
            sb.append(Randomly.fromList(indexes).getIndexName());
        }
        return new SQLQueryAdapter(sb.toString(), ExpectedErrors.from("no such index", "because the constraint"), true);
    }

}
