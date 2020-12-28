package sqlancer.monet;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.SQLConnection;
import sqlancer.SQLGlobalState;

public class MonetGlobalState extends SQLGlobalState<MonetOptions, MonetSchema> {

    public static final char IMMUTABLE = 'i';
    public static final char STABLE = 's';
    public static final char VOLATILE = 'v';

    private List<String> operators = Collections.emptyList();
    private List<String> collates = Collections.emptyList();
    private List<String> opClasses = Collections.emptyList();

    @Override
    public void setConnection(SQLConnection con) {
        super.setConnection(con);
        this.opClasses = Arrays.asList(new String[] {});
        this.operators = Arrays.asList(new String[] { "<", ">", "<=", ">=", "=", "<>", "!", "^", "|", "||", "&", "~",
                "+", "-", "/", "%", "*", "<<", ">>", "@" });
        this.collates = Arrays.asList(new String[] {});
    }

    public List<String> getOperators() {
        return operators;
    }

    public String getRandomOperator() {
        return Randomly.fromList(operators);
    }

    public List<String> getCollates() {
        return collates;
    }

    public String getRandomCollate() {
        return Randomly.fromList(collates);
    }

    public List<String> getOpClasses() {
        return opClasses;
    }

    public String getRandomOpclass() {
        return Randomly.fromList(opClasses);
    }

    @Override
    public MonetSchema readSchema() throws SQLException {
        return MonetSchema.fromConnection(getConnection(), getDatabaseName());
    }

}
