package sqlancer.monet.gen;

import java.util.HashSet;
import java.util.Set;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;

public final class MonetDeleteGenerator {

    private MonetDeleteGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        MonetTable table = globalState.getSchema().getRandomTable(t -> !t.isView());
        Set<String> errors = new HashSet<>();
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
