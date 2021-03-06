package sqlancer.monet.gen;

import java.util.Arrays;
import java.util.List;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetConstraint;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;

public class MonetAlterTableGenerator {

    private final MonetTable randomTable;
    // private Randomly r;
    private static MonetColumn randomColumn;
    private boolean generateOnlyKnown;
    // private List<String> opClasses;
    private final MonetGlobalState globalState;

    protected enum Action {
        ALTER_TABLE_ADD_COLUMN, // [ COLUMN ] column data_type [ COLLATE collation ] [
        // column_constraint [ ... ] ]
        ALTER_TABLE_DROP_COLUMN, // DROP [ COLUMN ] [ IF EXISTS ] column [ RESTRICT | CASCADE ]
        // ALTER_COLUMN_TYPE, // ALTER [ COLUMN ] column [ SET DATA ] TYPE data_type [ COLLATE collation ] [
        // USING expression ]
        ALTER_COLUMN_SET_DROP_DEFAULT, // ALTER [ COLUMN ] column SET DEFAULT expression and ALTER [ COLUMN ] column
                                       // DROP DEFAULT
        ALTER_COLUMN_SET_DROP_NULL, // ALTER [ COLUMN ] column { SET | DROP } NOT NULL
        // ALTER_COLUMN_SET_STORAGE, // ALTER [ COLUMN ] column SET STORAGE { PLAIN | EXTERNAL | EXTENDED | MAIN }
        ADD_TABLE_CONSTRAINT, // ADD table_constraint [ NOT VALID ]
        ALTER_TABLE_DROP_CONSTRAINT
    }

    public MonetAlterTableGenerator(MonetTable randomTable, MonetGlobalState globalState, boolean generateOnlyKnown) {
        this.randomTable = randomTable;
        this.globalState = globalState;
        this.generateOnlyKnown = generateOnlyKnown;
        // this.r = globalState.getRandomly();
        // this.opClasses = globalState.getOpClasses();
    }

    public static SQLQueryAdapter create(MonetTable randomTable, MonetGlobalState globalState, boolean generateOnlyKnown) {
        return new MonetAlterTableGenerator(randomTable, globalState, generateOnlyKnown).generate();
    }

    public List<Action> getActions(ExpectedErrors errors) {
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonInsertUpdateErrors(errors);
        MonetCommon.addCommonTableErrors(errors);

        errors.add("conversion of");
        errors.add("not supported on TEMPORARY table");
        errors.add("ALTER TABLE: can't alter temporary table");

        List<Action> action = Randomly.nonEmptySubset(Arrays.asList(Action.values()), 1);
        if (randomTable.getColumns().size() == 1) {
            action.remove(Action.ALTER_TABLE_DROP_COLUMN);
        }
        if (randomTable.getConstraints().size() == 0) {
            action.remove(Action.ALTER_TABLE_DROP_CONSTRAINT);
        }

        if (action.isEmpty()) {
            throw new IgnoreMeException();
        }
        return action;
    }

    public SQLQueryAdapter generate() {
        ExpectedErrors errors = new ExpectedErrors();
        int i = 0;
        List<Action> action = getActions(errors);
        StringBuilder sb = new StringBuilder();
        sb.append("ALTER TABLE ");
        sb.append(" ");
        sb.append(randomTable.getName());
        sb.append(" ");
        for (Action a : action) {
            if (i++ != 0) {
                sb.append(", ");
            }
            switch (a) {
            case ALTER_TABLE_ADD_COLUMN:
                int nextNumber = randomTable.getColumnCounter() + 1;
                String cname = String.format("c%d", nextNumber);

                MonetDataType type = MonetDataType.getRandomType();
                MonetColumn c = new MonetColumn(cname, type);
                randomTable.setColumnCounter(nextNumber);
                c.setTable(randomTable);
                randomTable.getColumns().add(c);

                sb.append("ADD COLUMN ");
                sb.append(cname);
                sb.append(" ");
                MonetCommon.appendDataType(type, sb, false, generateOnlyKnown, globalState.getCollates());
                /*sb.append(" "); no constraints for now
                if (Randomly.getBoolean()) {
                    createColumnConstraint(type, serial);
                }*/
                errors.add("already exists");
                break;
            case ALTER_TABLE_DROP_COLUMN:
                sb.append("DROP ");
                sb.append(randomTable.getRandomColumn().getName());
                errors.add("because other objects depend on it");
                if (Randomly.getBoolean()) {
                    sb.append(" ");
                    sb.append(Randomly.fromOptions("RESTRICT", "CASCADE"));
                }
                errors.add("does not exist");
                errors.add("cannot drop column");
                errors.add("cannot drop inherited column");
                break;
            /*
             * case ALTER_COLUMN_TYPE: alterColumn(randomTable, sb); if (Randomly.getBoolean()) {
             * sb.append(" SET DATA"); } sb.append(" TYPE "); MonetDataType randomType = MonetDataType.getRandomType();
             * MonetCommon.appendDataType(randomType, sb, false, generateOnlyKnown, opClasses); errors.add("types ");
             * errors.add("has no valid default value"); break;
             */
            case ALTER_COLUMN_SET_DROP_DEFAULT:
                alterColumn(randomTable, sb);
                if (Randomly.getBoolean()) {
                    sb.append("DROP DEFAULT");
                } else {
                    sb.append("SET DEFAULT ");
                    sb.append(MonetVisitor.asString(
                            MonetExpressionGenerator.generateExpression(globalState, randomColumn.getType())));
                }
                break;
            case ALTER_COLUMN_SET_DROP_NULL:
                alterColumn(randomTable, sb);
                if (Randomly.getBoolean()) {
                    sb.append("SET NULL");
                } else {
                    sb.append("SET NOT NULL");
                }
                errors.add("contains null values");
                errors.add("cannot change NOT NULL CONSTRAINT for column");
                errors.add("NOT NULL constraint violated for column");
                break;
            /*
             * case ALTER_COLUMN_SET_STORAGE: alterColumn(randomTable, sb); sb.append("SET STORAGE ");
             * sb.append(Randomly.fromOptions("PLAIN", "EXTERNAL", "EXTENDED", "MAIN")); break;
             */
            case ADD_TABLE_CONSTRAINT:
                int nextCNumber = randomTable.getColumnCounter() + 1;
                String conname = String.format("con%d", nextCNumber);

                MonetConstraint con = new MonetConstraint(conname);
                randomTable.setConstraintCounter(nextCNumber);
                randomTable.getConstraints().add(con);
    
                sb.append("ADD CONSTRAINT ");
                sb.append(String.format("con%d ", nextCNumber));
                MonetCommon.addTableConstraint(sb, randomTable, globalState, errors);

                errors.add("a table can have only one PRIMARY KEY");
                errors.add("already exists");
                break;
            case ALTER_TABLE_DROP_CONSTRAINT:
                sb.append("DROP CONSTRAINT ");
                sb.append(randomTable.getRandomConstraint().getName());
                errors.add("there are database objects which depend on it");
                if (Randomly.getBoolean()) {
                    sb.append(" ");
                    sb.append(Randomly.fromOptions("RESTRICT", "CASCADE"));
                }
                errors.add("does not exist");
                break;
            default:
                throw new AssertionError(a);
            }
        }

        return new SQLQueryAdapter(sb.toString(), errors, true);
    }

    private static void alterColumn(MonetTable randomTable, StringBuilder sb) {
        sb.append("ALTER ");
        randomColumn = randomTable.getRandomColumn();
        sb.append(randomColumn.getName());
        sb.append(" ");
    }

}
