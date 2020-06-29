package sqlancer.monet.gen;

import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetProvider;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;

public final class MonetCommon {

    private MonetCommon() {
    }

    public static void addCommonFetchErrors(Set<String> errors) {
    }

    public static void addCommonTableErrors(Set<String> errors) {
    }

    public static void addCommonExpressionErrors(Set<String> errors) {
        errors.add("LIKE pattern must not end with escape character");
        errors.add("division by zero");
        //errors.add("types ");
        errors.add("shift operand too large in ");
        errors.add("has no valid default value");
        errors.add("Wrong format (");
        errors.add(" no such binary operator");
        errors.add(" no such unary operator");
        errors.add("has incorrect format");
        errors.add("in query results without an aggregate function");
        errors.add("no such column");
        errors.add("field position must be greater than zero");
        errors.add("doesn't have format (");
        errors.add("to type int failed");
        errors.add(" no columns of tables ");
        errors.add("overflow in conversion of ");
        errors.add("overflow in calculation ");
        errors.add("number of characters for insert function must be non negative");
        errors.add("Too many digits");
        errors.add("conversion of");
        errors.add("value too long for type");
        errors.add("Numerical argument out of domain");
        errors.add("value too large or not a number in:");
        errors.add("value exceeds limits of type int");
        errors.add("overflow in calculation");
        errors.add("are not equal");
        addToCharFunctionErrors(errors);
        addBitStringOperationErrors(errors);
        addFunctionErrors(errors);
        addCommonRegexExpressionErrors(errors);
    }

    private static void addToCharFunctionErrors(Set<String> errors) {
        errors.add("is not a number");
    }

    private static void addBitStringOperationErrors(Set<String> errors) {
    }

    private static void addFunctionErrors(Set<String> errors) {
    }

    private static void addCommonRegexExpressionErrors(Set<String> errors) {
    }

    public static void addCommonInsertUpdateErrors(Set<String> errors) {
        errors.add("NOT NULL constraint violated for column");
        errors.add("SQL feature not yet available for expressions and default values");
        errors.add("value too large or not a number in");
        errors.add("PRIMARY KEY constraint");
        errors.add("UNIQUE constraint");
    }

    public static boolean appendDataType(MonetDataType type, StringBuilder sb, boolean allowSerial,
            boolean generateOnlyKnown, List<String> opClasses) throws AssertionError {
        boolean serial = false;
        switch (type) {
        case BOOLEAN:
            sb.append("boolean");
            break;
        case INT:
            if (Randomly.getBoolean() && allowSerial) {
                serial = true;
                sb.append(Randomly.fromOptions("serial", "bigserial"));
            } else {
                sb.append(Randomly.fromOptions("smallint", "integer", "bigint"));
            }
            break;
        case STRING:
            if (Randomly.getBoolean()) {
                sb.append("STRING");
            } else {
                // TODO: support CHAR (without VAR)
                if (MonetProvider.generateOnlyKnown || Randomly.getBoolean()) {
                    sb.append("VAR");
                }
                sb.append("CHAR");
                sb.append("(");
                sb.append(ThreadLocalRandom.current().nextInt(1, 500));
                sb.append(")");
            }
            break;
        case DECIMAL:
            sb.append("DECIMAL");
            break;
        case DOUBLE:
            sb.append("DOUBLE");
            break;
        case REAL:
            sb.append("FLOAT");
            break;
        default:
            throw new AssertionError(type);
        }
        return serial;
    }

    public enum TableConstraints {
        UNIQUE, PRIMARY_KEY, FOREIGN_KEY //, CHECK
    }

    public static void addTableConstraints(boolean excludePrimaryKey, StringBuilder sb, MonetTable table,
            MonetGlobalState globalState, Set<String> errors) {
        // TODO constraint name
        List<TableConstraints> tableConstraints = Randomly.nonEmptySubset(TableConstraints.values());
        if (excludePrimaryKey) {
            tableConstraints.remove(TableConstraints.PRIMARY_KEY);
        }
        if (globalState.getSchema().getDatabaseTables().isEmpty()) {
            tableConstraints.remove(TableConstraints.FOREIGN_KEY);
        }
        for (TableConstraints t : tableConstraints) {
            sb.append(", ");
            // TODO add index parameters
            addTableConstraint(sb, table, globalState, t, errors);
        }
    }

    public static void addTableConstraint(StringBuilder sb, MonetTable table, MonetGlobalState globalState,
            Set<String> errors) {
        addTableConstraint(sb, table, globalState, Randomly.fromOptions(TableConstraints.values()), errors);
    }

    private static void addTableConstraint(StringBuilder sb, MonetTable table, MonetGlobalState globalState,
            TableConstraints t, Set<String> errors) {
        List<MonetColumn> randomNonEmptyColumnSubset = table.getRandomNonEmptyColumnSubset();
        List<MonetColumn> otherColumns;
        MonetCommon.addCommonExpressionErrors(errors);
        switch (t) {
        /*case CHECK:
            sb.append("CHECK(");
            sb.append(MonetVisitor.getExpressionAsString(globalState, MonetDataType.BOOLEAN, table.getColumns()));
            sb.append(")");
            errors.add("constraint must be added to child tables too");
            errors.add("missing FROM-clause entry for table");
            break;*/
        case UNIQUE:
            sb.append("UNIQUE(");
            sb.append(randomNonEmptyColumnSubset.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
            sb.append(")");
            errors.add("already exists");
            //appendIndexParameters(sb, globalState, errors);
            break;
        case PRIMARY_KEY:
            sb.append("PRIMARY KEY(");
            sb.append(randomNonEmptyColumnSubset.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
            sb.append(")");
            //appendIndexParameters(sb, globalState, errors);
            errors.add("NOT NULL constraint violated for column");
            errors.add("a table can have only one PRIMARY KEY");
            break;
        case FOREIGN_KEY:
            sb.append("FOREIGN KEY (");
            sb.append(randomNonEmptyColumnSubset.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
            sb.append(") REFERENCES ");
            MonetTable randomOtherTable = globalState.getSchema().getRandomTable(tab -> !tab.isView());
            sb.append(randomOtherTable.getName());
            if (randomOtherTable.getColumns().size() < randomNonEmptyColumnSubset.size()) {
                throw new IgnoreMeException();
            }
            otherColumns = randomOtherTable.getRandomNonEmptyColumnSubset(randomNonEmptyColumnSubset.size());
            sb.append("(");
            sb.append(otherColumns.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
            sb.append(")");
            errors.add("could not find referenced PRIMARY KEY in table");
            if (Randomly.getBoolean()) {
                sb.append(Randomly.fromOptions(" MATCH FULL", " MATCH SIMPLE", " MATCH PARTIAL"));
            }
            /*if (Randomly.getBoolean()) {
                sb.append(" ON DELETE ");
                deleteOrUpdateAction(sb);
            }
            if (Randomly.getBoolean()) {
                sb.append(" ON UPDATE ");
                deleteOrUpdateAction(sb);
            }*/
            break;
        default:
            throw new AssertionError(t);
        }
    }

    /*private static void deleteOrUpdateAction(StringBuilder sb) {
        sb.append(Randomly.fromOptions("NO ACTION", "RESTRICT", "CASCADE", "SET NULL", "SET DEFAULT"));
    }*/

    public static void addGroupingErrors(Set<String> errors) {
        errors.add("must appear in the GROUP BY clause or be used in an aggregate function");
        errors.add("aggregate functions are not allowed in GROUP BY");
    }

}
