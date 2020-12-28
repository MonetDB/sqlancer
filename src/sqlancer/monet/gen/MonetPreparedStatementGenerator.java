package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetQuery;

public final class MonetPreparedStatementGenerator {

    private MonetPreparedStatementGenerator() {
    }

    public static SQLQueryAdapter create(MonetGlobalState globalState) {
        ExpectedErrors errors = new ExpectedErrors();

        StringBuilder sb = new StringBuilder("PREPARE ");
        MonetQuery select = MonetRandomQueryGenerator.createRandomQuery(0, Randomly.smallNumber() + 1, globalState,
                null, true, true, true);
        sb.append(MonetVisitor.asString(select));

        MonetCommon.addGroupingErrors(errors);
        MonetCommon.addCommonExpressionErrors(errors);
        errors.add("type missing");
        errors.add("Could not determine type for argument number");
        errors.add("parameter not allowed on");
        errors.add("Cannot have a parameter");
        errors.add("parameters not allowed");
        return new SQLQueryAdapter(sb.toString(), errors);
    }
}
