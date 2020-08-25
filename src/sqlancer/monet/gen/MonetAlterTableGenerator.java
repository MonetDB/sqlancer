package sqlancer.monet.gen;

import java.util.Arrays;
import java.util.List;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;

public class MonetAlterTableGenerator {

    private MonetTable randomTable;
    //private Randomly r;
    private static MonetColumn randomColumn;
    //private boolean generateOnlyKnown;
    //private List<String> opClasses;
    private MonetGlobalState globalState;

    protected enum Action {
        //ALTER_TABLE_ADD_COLUMN, // [ COLUMN ] column data_type [ COLLATE collation ] [
        // column_constraint [ ... ] ]
        ALTER_TABLE_DROP_COLUMN, // DROP [ COLUMN ] [ IF EXISTS ] column [ RESTRICT | CASCADE ]
        //ALTER_COLUMN_TYPE, // ALTER [ COLUMN ] column [ SET DATA ] TYPE data_type [ COLLATE collation ] [
                           // USING expression ]
        ALTER_COLUMN_SET_DROP_DEFAULT, // ALTER [ COLUMN ] column SET DEFAULT expression and ALTER [ COLUMN ] column
                                       // DROP DEFAULT
        ALTER_COLUMN_SET_DROP_NULL, // ALTER [ COLUMN ] column { SET | DROP } NOT NULL
        //ALTER_COLUMN_SET_STORAGE, // ALTER [ COLUMN ] column SET STORAGE { PLAIN | EXTERNAL | EXTENDED | MAIN }
        ADD_TABLE_CONSTRAINT // ADD table_constraint [ NOT VALID ]
    }

    public MonetAlterTableGenerator(MonetTable randomTable, MonetGlobalState globalState,
            boolean generateOnlyKnown) {
        this.randomTable = randomTable;
        this.globalState = globalState;
        //this.r = globalState.getRandomly();
        //this.generateOnlyKnown = generateOnlyKnown;
        //this.opClasses = globalState.getOpClasses();
    }

    public static Query create(MonetTable randomTable, MonetGlobalState globalState, boolean generateOnlyKnown) {
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
        if (action.isEmpty()) {
            throw new IgnoreMeException();
        }
        return action;
    }

    public Query generate() {
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
            /*case ALTER_COLUMN_TYPE:
                alterColumn(randomTable, sb);
                if (Randomly.getBoolean()) {
                    sb.append(" SET DATA");
                }
                sb.append(" TYPE ");
                MonetDataType randomType = MonetDataType.getRandomType();
                MonetCommon.appendDataType(randomType, sb, false, generateOnlyKnown, opClasses);
                errors.add("types ");
                errors.add("has no valid default value");
                break;*/
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
                sb.append("SET NOT NULL");
                errors.add("contains null values");
                errors.add("NOT NULL constraint violated for column");
                break;
            /*case ALTER_COLUMN_SET_STORAGE:
                alterColumn(randomTable, sb);
                sb.append("SET STORAGE ");
                sb.append(Randomly.fromOptions("PLAIN", "EXTERNAL", "EXTENDED", "MAIN"));
                break;*/
            case ADD_TABLE_CONSTRAINT:
                sb.append("ADD ");
                MonetCommon.addTableConstraint(sb, randomTable, globalState, errors);

                //errors.add("types ");
                errors.add("shift operand too large in ");
                errors.add("has no valid default value");
                errors.add("value too long for type (var)char");
                errors.add("NOT NULL constraint violated for column");
                errors.add("a table can have only one PRIMARY KEY");
                errors.add("already exists");
                break;
            default:
                throw new AssertionError(a);
            }
        }

        return new QueryAdapter(sb.toString(), errors, true);
    }

    private static void alterColumn(MonetTable randomTable, StringBuilder sb) {
        sb.append("ALTER ");
        randomColumn = randomTable.getRandomColumn();
        sb.append(randomColumn.getName());
        sb.append(" ");
    }

}
