package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;

public final class MonetLoggerSuspenderGenerator {

    private MonetLoggerSuspenderGenerator() {
    }

    public static Query create(MonetGlobalState globalState) {
        StringBuilder sb = new StringBuilder("CALL sys.");
        sb.append(Randomly.fromOptions("flush_log", "suspend_log_flushing", "resume_log_flushing"));
        sb.append("()");

        return new QueryAdapter(sb.toString(), false);
    }

}
