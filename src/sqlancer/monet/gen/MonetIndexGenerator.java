package sqlancer.monet.gen;

import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetIndex;
import sqlancer.monet.MonetSchema.MonetTable;

import sqlancer.sqlite3.gen.SQLite3Common;

public final class MonetIndexGenerator {

    private MonetIndexGenerator() {
    }

    public enum IndexType {
        UNIQUE, ORDERED, IMPRINTS, HASH
    }

    public static Query generate(MonetGlobalState globalState) {
        ExpectedErrors errors = new ExpectedErrors();
        StringBuilder sb = new StringBuilder();
        sb.append("CREATE ");
        IndexType method = Randomly.fromOptions(IndexType.values());
        if (method != IndexType.HASH) {
            sb.append(method.toString());
        }
        sb.append(" INDEX ");
        MonetTable randomTable = globalState.getSchema().getRandomTable(t -> !t.isView());
        String indexName = getNewIndexName(randomTable);
        sb.append(indexName);
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
        MonetCommon.addCommonExpressionErrors(errors);
        return new QueryAdapter(sb.toString(), errors);
    }

    private static String getNewIndexName(MonetTable randomTable) {
        List<MonetIndex> indexes = randomTable.getIndexes();
        int indexI = 0;
        while (true) {
            String indexName = SQLite3Common.createIndexName(indexI++);
            if (indexes.stream().noneMatch(i -> i.getIndexName().equals(indexName))) {
                return indexName;
            }
        }
    }

}
