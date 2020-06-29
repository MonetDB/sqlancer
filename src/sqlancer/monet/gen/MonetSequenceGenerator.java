package sqlancer.monet.gen;

import java.util.ArrayList;
import java.util.List;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;

public final class MonetSequenceGenerator {

    private MonetSequenceGenerator() {
    }

    public static Query createSequence(MonetGlobalState globalState) {
        List<String> errors = new ArrayList<>();
        StringBuilder sb = new StringBuilder("CREATE");
        /*if (Randomly.getBoolean()) {
            sb.append(" ");
            sb.append(Randomly.fromOptions("TEMPORARY", "TEMP"));
        }*/
        sb.append(" SEQUENCE ");
        // TODO keep track of sequences
        //sb.append(" IF NOT EXISTS");
        // TODO generate sequence names
        sb.append(" seq");
        if (Randomly.getBoolean()) {
            sb.append(" AS ");
            sb.append(Randomly.fromOptions("smallint", "integer", "bigint"));
        }
        if (Randomly.getBoolean()) {
            sb.append(" INCREMENT BY ");
            sb.append(globalState.getRandomly().getInteger());
            errors.add("INCREMENT must not be zero");
        }
        if (Randomly.getBoolean()) {
            if (Randomly.getBoolean()) {
                sb.append(" MINVALUE");
                sb.append(" ");
                sb.append(globalState.getRandomly().getInteger());
            } else {
                sb.append(" NO MINVALUE");
            }
            errors.add("must be less than MAXVALUE");

            errors.add("START value is higher than MAXVALUE");
        }
        if (Randomly.getBoolean()) {
            if (Randomly.getBoolean()) {
                sb.append(" MAXVALUE");
                sb.append(" ");
                sb.append(globalState.getRandomly().getInteger());
            } else {
                sb.append(" NO MAXVALUE");
            }
            errors.add("must be less than MAXVALUE");

            errors.add("START value is higher than MAXVALUE");
        }
        if (Randomly.getBoolean()) {
            sb.append(" START");
            if (Randomly.getBoolean()) {
                sb.append(" WITH");
            }
            sb.append(" ");
            sb.append(globalState.getRandomly().getInteger());
            errors.add("cannot be less than MINVALUE");
            errors.add("cannot be greater than MAXVALUE");
        }
        if (Randomly.getBoolean()) {
            sb.append(" CACHE ");
            sb.append(globalState.getRandomly().getPositiveIntegerNotNull());
        }
        errors.add("is out of range");
        if (Randomly.getBoolean()) {
            if (Randomly.getBoolean()) {
                sb.append(" NO");
            }
            sb.append(" CYCLE");
        }
        return new QueryAdapter(sb.toString(), errors);
    }

}
