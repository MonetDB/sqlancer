package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;

public final class MonetDeleteGenerator {

    private MonetDeleteGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        MonetTable table = globalState.getSchema().getRandomTable(t -> !t.isView());
        ExpectedErrors errors = new ExpectedErrors();
        MonetCommon.addCommonInsertUpdateErrors(errors);
        StringBuilder sb = new StringBuilder("DELETE FROM");
        sb.append(" ");
        sb.append(table.getName());
        if (Randomly.getBoolean()) {
            sb.append(" WHERE ");
            sb.append(MonetVisitor.asString(MonetExpressionGenerator.generateExpression(globalState,
                    table.getColumns(), MonetDataType.BOOLEAN)));
        }
        MonetCommon.addCommonExpressionErrors(errors);
        errors.add("division by zero");
        errors.add("conversion of");
        errors.add("cannot delete view");
        return new QueryAdapter(sb.toString(), errors);
    }

}
