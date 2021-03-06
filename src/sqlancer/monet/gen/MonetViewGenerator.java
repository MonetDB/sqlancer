package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetQuery;

public final class MonetViewGenerator {

    private MonetViewGenerator() {
    }

    public static SQLQueryAdapter create(MonetGlobalState globalState) {
        ExpectedErrors errors = new ExpectedErrors();
        StringBuilder sb = new StringBuilder("CREATE");
        sb.append(" VIEW ");
        int i = 0;
        String[] name = new String[1];
        while (true) {
            name[0] = "v" + i++;
            if (globalState.getSchema().getDatabaseTables().stream()
                    .noneMatch(tab -> tab.getName().contentEquals(name[0]))) {
                break;
            }
        }
        sb.append(name[0]);
        sb.append("(");
        int nrColumns = Randomly.smallNumber() + 1;
        for (i = 0; i < nrColumns; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            sb.append(String.format("vc%d", i));
        }
        sb.append(")");
        sb.append(" AS (");
        MonetQuery select = MonetRandomQueryGenerator.createRandomQuery(0, nrColumns, globalState, null, true, false,
                false);
        sb.append(MonetVisitor.asString(select));
        sb.append(")");
        if (Randomly.getBoolean()) {
            sb.append(" WITH CHECK OPTION");
        }
        MonetCommon.addGroupingErrors(errors);
        errors.add("already exists");
        errors.add("specified more than once"); // TODO
        errors.add("is not a view");
        errors.add("LIMIT not supported");
        errors.add("SAMPLE not supported");
        errors.add("no such table");
        MonetCommon.addCommonExpressionErrors(errors);
        return new SQLQueryAdapter(sb.toString(), errors, true);
    }

}
