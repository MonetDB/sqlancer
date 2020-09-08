package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetSelect;

public class MonetPreparedStatementGenerator {

    private MonetPreparedStatementGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        ExpectedErrors errors = new ExpectedErrors();

        StringBuilder sb = new StringBuilder("PREPARE ");
        MonetSelect select = MonetRandomQueryGenerator.createRandomQuery(0, Randomly.smallNumber() + 1, globalState, true, true, true);
        sb.append(MonetVisitor.asString(select));

        MonetCommon.addGroupingErrors(errors);
        MonetCommon.addCommonExpressionErrors(errors);
        errors.add("type missing");
        errors.add("Could not determine type for argument number");
        errors.add("parameter not allowed on");
        errors.add("Cannot have a parameter");
        return new QueryAdapter(sb.toString(), errors, true);
    }
}
