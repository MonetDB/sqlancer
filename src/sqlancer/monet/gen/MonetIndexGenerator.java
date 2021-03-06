package sqlancer.monet.gen;

import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetIndex;
import sqlancer.monet.MonetSchema.MonetTable;

public final class MonetIndexGenerator {

    private MonetIndexGenerator() {
    }

    public enum IndexType {
        UNIQUE, ORDERED, IMPRINTS, HASH
    }

    public static SQLQueryAdapter generate(MonetGlobalState globalState) {
        ExpectedErrors errors = new ExpectedErrors();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ");
        IndexType method = Randomly.fromOptions(IndexType.values());
        if (method != IndexType.HASH) {
            sb.append(method.toString());
            sb.append(" ");
        }
        sb.append("INDEX ");
        MonetTable randomTable = globalState.getSchema().getRandomTable(t -> !t.isView());
        int nextINumber = randomTable.getIndexCounter() + 1;
        MonetIndex index = MonetIndex.create(String.format("i%d", nextINumber));
        sb.append(index.getIndexName());
        randomTable.setIndexCounter(nextINumber);
        randomTable.getIndexes().add(index);
        sb.append(" ON ");
        sb.append(randomTable.getName());
        sb.append(" (");
        List<MonetColumn> columns = randomTable.getRandomNonEmptyColumnSubset();
        sb.append(columns.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
        sb.append(")");
        errors.add("division by zero");
        errors.add("out of range");
        errors.add("conversion of");
        errors.add("already in use");
        errors.add("unsupported type");
        errors.add("name already in use");
        MonetCommon.addCommonExpressionErrors(errors);
        return new SQLQueryAdapter(sb.toString(), errors, true);
    }
}
