package sqlancer.monet.gen;

import java.util.ArrayList;

import sqlancer.Query;
import sqlancer.QueryAdapter;

public final class MonetTransactionGenerator {

    private MonetTransactionGenerator() {
    }

    public static Query executeBegin() {
        return new QueryAdapter("START TRANSACTION", new ArrayList<>(), true);
    }

}
