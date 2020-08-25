package sqlancer.monet.gen;

import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;

public final class MonetTransactionGenerator {

    private MonetTransactionGenerator() {
    }

    public static Query executeBegin() {
        return new QueryAdapter("START TRANSACTION", new ExpectedErrors(), true);
    }

}
