package sqlancer.monet;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import sqlancer.GlobalState;
import sqlancer.Randomly;

public class MonetGlobalState extends GlobalState<MonetOptions, MonetSchema> {

    public static final char IMMUTABLE = 'i';
    public static final char STABLE = 's';
    public static final char VOLATILE = 'v';

    private List<String> operators = Collections.emptyList();
    private List<String> collates = Collections.emptyList();
    private List<String> opClasses = Collections.emptyList();

    @Override
    public void setConnection(Connection con) {
        super.setConnection(con);
        try {
            this.opClasses = getOpclasses(getConnection());
            this.operators = getOperators(getConnection());
            this.collates = getCollnames(getConnection());
        } catch (SQLException e) {
            throw new AssertionError(e);
        }
    }

    private List<String> getCollnames(Connection con) throws SQLException {
        List<String> opClasses = Arrays.asList(new String[] {});
        return opClasses;
    }

    private List<String> getOpclasses(Connection con) throws SQLException {
        List<String> opClasses = Arrays.asList(new String[] {});
        return opClasses;
    }

    private List<String> getOperators(Connection con) throws SQLException {
        List<String> opClasses = Arrays.asList(new String[] {"<", ">", "<=", ">=", "=", "<>", "!", "^", "|", "||", "&", "~", "+", "-", "/", "%", "*", "<<", ">>", "@"});
        return opClasses;
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
