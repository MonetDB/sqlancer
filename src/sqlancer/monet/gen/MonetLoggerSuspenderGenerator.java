package sqlancer.monet.gen;

import sqlancer.Randomly;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;

public final class MonetLoggerSuspenderGenerator {

    private MonetLoggerSuspenderGenerator() {
    }

    public static SQLQueryAdapter create(MonetGlobalState globalState) {
        StringBuilder sb = new StringBuilder("CALL sys.");
        sb.append(Randomly.fromOptions("flush_log", "suspend_log_flushing", "resume_log_flushing"));
        sb.append("()");

        return new SQLQueryAdapter(sb.toString(), false);
    }

}
