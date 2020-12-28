package sqlancer.monet.gen;

import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;

public final class MonetTransactionGenerator {

    private MonetTransactionGenerator() {
    }

    public static SQLQueryAdapter executeBegin() {
        return new SQLQueryAdapter("START TRANSACTION", new ExpectedErrors(), true);
    }

}
