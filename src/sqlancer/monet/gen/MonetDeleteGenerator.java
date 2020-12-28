package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;

public final class MonetDeleteGenerator {

    private MonetDeleteGenerator() {
    }

    public static SQLQueryAdapter create(MonetGlobalState globalState) {
        MonetTable table = globalState.getSchema().getRandomTable(t -> t.isInsertable());
        ExpectedErrors errors = new ExpectedErrors();
        MonetCommon.addCommonInsertUpdateErrors(errors);
        StringBuilder sb = new StringBuilder("DELETE FROM");
        sb.append(" ");
        sb.append(table.getName());
        if (Randomly.getBoolean()) {
            sb.append(" WHERE ");
            sb.append(MonetVisitor.asString(MonetExpressionGenerator.generateExpression(globalState, table.getColumns(),
                    MonetDataType.BOOLEAN)));
        }
        MonetCommon.addCommonExpressionErrors(errors);
        errors.add("division by zero");
        errors.add("conversion of");
        errors.add("cannot delete view");
        return new SQLQueryAdapter(sb.toString(), errors);
    }

}
