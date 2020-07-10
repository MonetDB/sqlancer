package sqlancer.monet.gen;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import sqlancer.Query;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;
import sqlancer.sqlite3.gen.SQLite3Common;

public class MonetTableGenerator {

    private final String tableName;
    private boolean columnCanHavePrimaryKey;
    private boolean columnHasPrimaryKey;
    private final StringBuilder sb = new StringBuilder();
    private boolean isTemporaryTable;
    private final MonetSchema newSchema;
    private final List<MonetColumn> columnsToBeAdded = new ArrayList<>();
    private final Set<String> errors = new HashSet<>();
    private final MonetTable table;
    private final boolean generateOnlyKnown;
    private final MonetGlobalState globalState;

    public MonetTableGenerator(String tableName, MonetSchema newSchema, boolean generateOnlyKnown,
            MonetGlobalState globalState) {
        this.tableName = tableName;
        this.newSchema = newSchema;
        this.generateOnlyKnown = generateOnlyKnown;
        this.globalState = globalState;
        table = new MonetTable(tableName, columnsToBeAdded, null, null, null, false, false);
        errors.add("invalid input syntax for");
        errors.add("is not unique");
        errors.add("integer out of range");
        errors.add("division by zero");
        errors.add("cannot cast");

        errors.add("conversion of");
        errors.add("SQL feature not yet available for expressions and default values");
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonTableErrors(errors);
    }

    public static Query generate(String tableName, MonetSchema newSchema, boolean generateOnlyKnown,
            MonetGlobalState globalState) {
        return new MonetTableGenerator(tableName, newSchema, generateOnlyKnown, globalState).generate();
    }

    private Query generate() {
        columnCanHavePrimaryKey = true;
        sb.append("CREATE");
        if (Randomly.getBoolean()) {
            sb.append(" ");
            isTemporaryTable = true;
            sb.append(Randomly.fromOptions("TEMPORARY", "TEMP"));
        }
        sb.append(" TABLE");
        if (Randomly.getBoolean()) {
            sb.append(" IF NOT EXISTS");
        }
        sb.append(" ");
        sb.append(tableName);
        if (Randomly.getBoolean() && !newSchema.getDatabaseTables().isEmpty()) {
            createLike();
        } else {
            createStandard();
        }
        return new QueryAdapter(sb.toString(), errors, true);
    }

    private void createStandard() throws AssertionError {
        sb.append("(");
        for (int i = 0; i < Randomly.smallNumber() + 1; i++) {
            if (i != 0) {
                sb.append(", ");
            }
            String name = SQLite3Common.createColumnName(i);
            createColumn(name);
        }
        if (Randomly.getBoolean()) {
            MonetCommon.addTableConstraints(columnHasPrimaryKey, sb, table, globalState, errors);
        }
        sb.append(")");
        //MonetCommon.generateWith(sb, globalState, errors);
        if (Randomly.getBoolean() && isTemporaryTable) {
            sb.append(" ON COMMIT ");
            sb.append(Randomly.fromOptions("PRESERVE ROWS", "DELETE ROWS", "DROP"));
            sb.append(" ");
        }
    }

    private void createLike() {
        sb.append("(");
        sb.append("LIKE ");
        sb.append(newSchema.getRandomTable().getName());
        sb.append(")");
    }

    private void createColumn(String name) throws AssertionError {
        sb.append(name);
        sb.append(" ");
        MonetDataType type = MonetDataType.getRandomType();
        boolean serial = MonetCommon.appendDataType(type, sb, true, generateOnlyKnown, globalState.getCollates());
        MonetColumn c = new MonetColumn(name, type);
        c.setTable(table);
        columnsToBeAdded.add(c);
        sb.append(" ");
        if (Randomly.getBoolean()) {
            createColumnConstraint(type, serial);
        }
    }

    private enum ColumnConstraint {
        NULL_OR_NOT_NULL, UNIQUE, PRIMARY_KEY, DEFAULT, GENERATED
    };

    private void createColumnConstraint(MonetDataType type, boolean serial) {
        List<ColumnConstraint> constraintSubset = Randomly.nonEmptySubset(ColumnConstraint.values());
        /*if (Randomly.getBoolean()) {
            // make checks constraints less likely
            constraintSubset.remove(ColumnConstraint.CHECK);
        }*/
        if (!columnCanHavePrimaryKey || columnHasPrimaryKey) {
            constraintSubset.remove(ColumnConstraint.PRIMARY_KEY);
        }
        if (constraintSubset.contains(ColumnConstraint.GENERATED)
                && constraintSubset.contains(ColumnConstraint.DEFAULT)) {
            // otherwise: ERROR: both default and identity specified for column
            constraintSubset.remove(Randomly.fromOptions(ColumnConstraint.GENERATED, ColumnConstraint.DEFAULT));
        }
        if (constraintSubset.contains(ColumnConstraint.GENERATED) && type != MonetDataType.INT) {
            // otherwise: ERROR: identity column type must be smallint, integer, or bigint
            constraintSubset.remove(ColumnConstraint.GENERATED);
        }
        if (serial) {
            constraintSubset.remove(ColumnConstraint.GENERATED);
            constraintSubset.remove(ColumnConstraint.DEFAULT);
            constraintSubset.remove(ColumnConstraint.NULL_OR_NOT_NULL);

        }
        for (ColumnConstraint c : constraintSubset) {
            sb.append(" ");
            switch (c) {
            case NULL_OR_NOT_NULL:
                sb.append(Randomly.fromOptions("NOT NULL", "NULL"));
                break;
            case UNIQUE:
                sb.append("UNIQUE");
                errors.add("already exists");
                break;
            case PRIMARY_KEY:
                sb.append("PRIMARY KEY");
                errors.add("NOT NULL constraint violated for column");
                errors.add("a table can have only one PRIMARY KEY");
                columnHasPrimaryKey = true;
                break;
            case DEFAULT:
                sb.append("DEFAULT");
                sb.append(" (");
                sb.append(MonetVisitor.asString(MonetExpressionGenerator.generateExpression(globalState, type)));
                sb.append(")");
                errors.add("out of range");
                errors.add("is a generated column");
                break;
            /*case CHECK:
                sb.append("CHECK (");
                sb.append(MonetVisitor.asString(MonetExpressionGenerator.generateExpression(globalState,
                        columnsToBeAdded, MonetDataType.BOOLEAN)));
                sb.append(")");
                errors.add("out of range");
                break;*/
            case GENERATED:
                if (Randomly.getBoolean()) {
                    sb.append("AUTO_INCREMENT");
                } else {
                    sb.append("GENERATED ALWAYS AS IDENTITY");
                }
                break;
            default:
                throw new AssertionError(sb);
            }
        }
    }

}
