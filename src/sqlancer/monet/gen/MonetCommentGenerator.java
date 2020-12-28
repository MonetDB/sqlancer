package sqlancer.monet.gen;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.ast.MonetConstant;

/**
 * @see https://www.postgresql.org/docs/devel/sql-comment.html
 */
public final class MonetCommentGenerator {

    private MonetCommentGenerator() {
    }

    private enum Action {
        INDEX, COLUMN, TABLE
    }

    public static SQLQueryAdapter generate(MonetGlobalState globalState) {
        StringBuilder sb = new StringBuilder();
        sb.append("COMMENT ON ");
        Action type = Randomly.fromOptions(Action.values());
        MonetTable randomTable = globalState.getSchema().getRandomTable();
        switch (type) {
        case INDEX:
            sb.append("INDEX ");
            if (randomTable.getIndexes().isEmpty()) {
                throw new IgnoreMeException();
            } else {
                sb.append(randomTable.getRandomIndex().getIndexName());
            }
            break;
        case COLUMN:
            sb.append("COLUMN ");
            sb.append(randomTable.getRandomColumn().getFullQualifiedName());
            break;
        case TABLE:
            sb.append("TABLE ");
            if (randomTable.isView()) {
                throw new IgnoreMeException();
            }
            sb.append(randomTable.getName());
            break;
        default:
            throw new AssertionError(type);
        }
        sb.append(" IS ");
        if (Randomly.getBoolean()) {
            sb.append("NULL");
        } else {
            sb.append(MonetConstant.createTextConstant(globalState.getRandomly().getString()).getTextRepresentation());
        }
        return new SQLQueryAdapter(sb.toString(), ExpectedErrors.from("no such table", "no such column", "no such index", "COMMENT ON tmp object not allowed"), true);
    }

}
