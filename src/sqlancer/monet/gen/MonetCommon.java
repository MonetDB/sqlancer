package sqlancer.monet.gen;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetProvider;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;

public final class MonetCommon {

    private MonetCommon() {
    }

    public static void addCommonFetchErrors(ExpectedErrors errors) {
    }

    public static void addCommonTableErrors(ExpectedErrors errors) {
    }

    public static void addCommonExpressionErrors(ExpectedErrors errors) {
        errors.add("LIKE pattern must not end with escape character");
        errors.add("by zero"); /* Divisions by zero */
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
        errors.add("overflow in ");
        errors.add("number of characters for insert function must be non negative");
        errors.add("many digits");
        errors.add("onversion of");
        errors.add("value too long for type");
        errors.add("Numerical argument out of domain");
        errors.add("value too large or not a number in");
        errors.add("value exceeds limits of type");
        errors.add("Value too large to fit at");
        errors.add("are not equal");
        errors.add("Numerical result out of range");
        errors.add("Argument 2 to round function must be positive");
        errors.add("Overflow");
        errors.add("Invalid result");
        errors.add("is not a number");
        errors.add("on both sides of the JOIN expression");
        errors.add("specified more than once");
        errors.add("more than one match");
        errors.add("scalar value expected");
        errors.add("scalar expression expected");
        errors.add("on both sides of the");
        errors.add("common column name");
        errors.add("ambiguous");
        errors.add(" overflows type");
        errors.add("is not in the number of projections range");
        errors.add("Not a UUID");
        errors.add("atom2sql");
        errors.add("Rounding of decimal");
        errors.add("Result too large");
        errors.add("Domain error");
        errors.add("Digits out of bounds");
        /* TODO the following errors should be removed */
        errors.add("Decimal ");
        errors.add("Timestamp ");
        errors.add("Date ");
        errors.add("Daytime ");
        errors.add("Time ");
    }

    public static void addCommonInsertUpdateErrors(ExpectedErrors errors) {
        errors.add("NOT NULL constraint violated for column");
        errors.add("SQL feature not yet available for expressions and default values");
        errors.add("not implemented as");
        errors.add("value too large or not a number in");
        errors.add("PRIMARY KEY constraint");
        errors.add("UNIQUE constraint");
        errors.add("FOREIGN KEY constraint");
    }

    public static boolean appendDataType(MonetDataType type, StringBuilder sb, boolean allowSerial,
            boolean generateOnlyKnown, List<String> opClasses) throws AssertionError {
        boolean serial = false;
        switch (type) {
        case BOOLEAN:
            sb.append("boolean");
            break;
        case TINYINT:
            sb.append("tinyint");
            break;
        case SMALLINT:
            sb.append("smallint");
            break;
        case INT:
            if (Randomly.getBoolean() && allowSerial) {
                serial = true;
                sb.append("serial");
            } else {
                sb.append(Randomly.fromOptions("int", "integer"));
            }
            break;
        case BIGINT:
            if (Randomly.getBoolean() && allowSerial) {
                serial = true;
                sb.append("bigserial");
            } else {
                sb.append("bigint");
            }
            break;
        case HUGEINT:
            sb.append("hugeint");
            break;
        case STRING:
            if (Randomly.getBoolean()) {
                sb.append(Randomly.fromOptions("STRING", "CLOB"));
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
            sb.append(Randomly.fromOptions("DOUBLE", "DOUBLE PRECISION"));
            break;
        case REAL:
            sb.append(Randomly.fromOptions("FLOAT", "REAL"));
            break;
        case TIME:
            sb.append("TIME");
            break;
        case TIMESTAMP:
            sb.append("TIMESTAMP");
            break;
        case DATE:
            sb.append("DATE");
            break;
        case SECOND_INTERVAL:
            sb.append("INTERVAL SECOND");
            break;
        case DAY_INTERVAL:
            sb.append("INTERVAL DAY");
            break;
        case MONTH_INTERVAL:
            sb.append("INTERVAL MONTH");
            break;
        case BLOB:
            sb.append("BLOB");
            break;
        case UUID:
            sb.append("UUID");
            break;
        default:
            throw new AssertionError(type);
        }
        return serial;
    }

    public enum TableConstraints {
        UNIQUE, PRIMARY_KEY, FOREIGN_KEY // , CHECK
    }

    public static void addTableConstraints(boolean excludePrimaryKey, StringBuilder sb, MonetTable table,
            MonetGlobalState globalState, ExpectedErrors errors) {
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
            ExpectedErrors errors) {
        addTableConstraint(sb, table, globalState, Randomly.fromOptions(TableConstraints.values()), errors);
    }

    private static void addTableConstraint(StringBuilder sb, MonetTable table, MonetGlobalState globalState,
            TableConstraints t, ExpectedErrors errors) {
        List<MonetColumn> randomNonEmptyColumnSubset = table.getRandomNonEmptyColumnSubset();
        List<MonetColumn> otherColumns;
        MonetCommon.addCommonExpressionErrors(errors);
        switch (t) {
        /*
         * case CHECK: sb.append("CHECK("); sb.append(MonetVisitor.getExpressionAsString(globalState,
         * MonetDataType.BOOLEAN, table.getColumns())); sb.append(")");
         * errors.add("constraint must be added to child tables too");
         * errors.add("missing FROM-clause entry for table"); break;
         */
        case UNIQUE:
            sb.append("UNIQUE(");
            sb.append(randomNonEmptyColumnSubset.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
            sb.append(")");
            errors.add("already exists");
            // appendIndexParameters(sb, globalState, errors);
            break;
        case PRIMARY_KEY:
            sb.append("PRIMARY KEY(");
            sb.append(randomNonEmptyColumnSubset.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
            sb.append(")");
            // appendIndexParameters(sb, globalState, errors);
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
            errors.add("could not find referenced");
            errors.add("is not compatible with the referenced");
            if (Randomly.getBoolean()) {
                sb.append(Randomly.fromOptions(" MATCH FULL", " MATCH SIMPLE", " MATCH PARTIAL"));
            }
            /*
             * if (Randomly.getBoolean()) { sb.append(" ON DELETE "); deleteOrUpdateAction(sb); } if
             * (Randomly.getBoolean()) { sb.append(" ON UPDATE "); deleteOrUpdateAction(sb); }
             */
            break;
        default:
            throw new AssertionError(t);
        }
    }

    /*
     * private static void deleteOrUpdateAction(StringBuilder sb) { sb.append(Randomly.fromOptions("NO ACTION",
     * "RESTRICT", "CASCADE", "SET NULL", "SET DEFAULT")); }
     */

    public static void addGroupingErrors(ExpectedErrors errors) {
        errors.add("must appear in the GROUP BY clause or be used in an aggregate function");
        errors.add("aggregate functions are not allowed in GROUP BY");
    }

}
