package sqlancer.monet.gen;

import java.util.ArrayList;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetExpression;

public final class MonetUpdateGenerator {

    private MonetUpdateGenerator() {
    }

    public static SQLQueryAdapter create(MonetGlobalState globalState) {
        MonetTable randomTable = globalState.getSchema().getRandomTable(t -> t.isInsertable()), randomTable2 = null;
        StringBuilder sb = new StringBuilder();
        sb.append("UPDATE ");
        sb.append(randomTable.getName());
        sb.append(" SET ");
        ExpectedErrors errors = ExpectedErrors.from("reached maximum value of sequence",
                "violates foreign key constraint", "violates not-null constraint", "violates unique constraint",
                "out of range", "cannot cast", "must be type boolean", "division by zero",
                "You might need to add explicit type casts.", "invalid regular expression");
        errors.add("multiple assignments to same column"); // view whose columns refer to a column in the referenced
                                                           // table multiple times
        List<MonetColumn> columns = randomTable.getRandomNonEmptyColumnSubset();
        MonetCommon.addCommonInsertUpdateErrors(errors);
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState);
        List<MonetColumn> cols = new ArrayList<>(randomTable.getColumns().size());
        cols.addAll(randomTable.getColumns());

        if (!Randomly.getBooleanWithSmallProbability()) { /* TODO the same table may be picked again, so use an alias */
            randomTable2 = globalState.getSchema().getRandomTable();
            cols.addAll(randomTable2.getColumns());
        }
        gen.setColumns(cols);

        for (int i = 0; i < columns.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            MonetColumn column = columns.get(i);
            sb.append(column.getName());
            sb.append(" = ");
            if (!Randomly.getBoolean()) {
                MonetExpression exp = gen.generateExpression(column.getType());
                sb.append(MonetVisitor.asString(exp));
            } else if (Randomly.getBoolean()) {
                sb.append("DEFAULT");
            } else {
                sb.append("(");
                MonetExpression expr = gen.generateExpression(column.getType());
                // caused by casts
                sb.append(MonetVisitor.asString(expr));
                sb.append(")");
            }
        }
        errors.add("invalid input syntax for ");
        errors.add("but expression is of type");
        errors.add("conversion of");
        errors.add("cannot update view");

        if (randomTable2 != null) {
            sb.append(" FROM ");
            sb.append(randomTable2.getName());
        }

        MonetCommon.addCommonExpressionErrors(errors);
        if (!Randomly.getBooleanWithSmallProbability()) {
            sb.append(" WHERE ");
            MonetExpression where = gen.generateExpression(MonetDataType.BOOLEAN);
            sb.append(MonetVisitor.asString(where));
        }

        return new SQLQueryAdapter(sb.toString(), errors);
    }

}
