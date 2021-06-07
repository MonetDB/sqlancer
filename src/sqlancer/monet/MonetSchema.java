package sqlancer.monet;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.SQLConnection;
import sqlancer.common.schema.AbstractRelationalTable;
import sqlancer.common.schema.AbstractRowValue;
import sqlancer.common.schema.AbstractSchema;
import sqlancer.common.schema.AbstractTableColumn;
import sqlancer.common.schema.AbstractTables;
import sqlancer.common.schema.TableIndex;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetSchema.MonetTable.TableType;
import sqlancer.monet.ast.MonetConstant;

public class MonetSchema extends AbstractSchema<MonetGlobalState, MonetTable> {

    private final String databaseName;

    public enum MonetDataType {
        TINYINT, SMALLINT, INT, BIGINT, HUGEINT, BOOLEAN, STRING, DECIMAL, REAL, DOUBLE, TIME, TIMESTAMP, DATE,
        SECOND_INTERVAL, DAY_INTERVAL, MONTH_INTERVAL, BLOB, UUID;

        private static String oS = System.getProperty("os.name").toLowerCase();

        private static List<MonetDataType> numericTypes;

        private static List<MonetDataType> allTypes;

        public static void intitializeTypes() {
            allTypes = new ArrayList<>(Arrays.asList(values()));

            if (MonetProvider.GENERATE_ONLY_KNOWN || oS.contains("win")) {
                allTypes.remove(MonetDataType.HUGEINT); /* No huge integers on Windows :( */
            }
            if (MonetProvider.GENERATE_ONLY_KNOWN) {
                allTypes.remove(MonetDataType.TINYINT);
                allTypes.remove(MonetDataType.SMALLINT);
                allTypes.remove(MonetDataType.BIGINT);
                allTypes.remove(MonetDataType.DECIMAL);
                allTypes.remove(MonetDataType.DOUBLE);
                allTypes.remove(MonetDataType.REAL);
                allTypes.remove(MonetDataType.BOOLEAN);
                allTypes.remove(MonetDataType.TIME);
                allTypes.remove(MonetDataType.TIMESTAMP);
                allTypes.remove(MonetDataType.DATE);
                allTypes.remove(MonetDataType.SECOND_INTERVAL);
                allTypes.remove(MonetDataType.DAY_INTERVAL);
                allTypes.remove(MonetDataType.MONTH_INTERVAL);
                allTypes.remove(MonetDataType.BLOB);
                allTypes.remove(MonetDataType.UUID);
            }

            numericTypes = new ArrayList<>();

            numericTypes.add(MonetDataType.INT);
            if (!MonetProvider.GENERATE_ONLY_KNOWN) {
                numericTypes.add(MonetDataType.TINYINT);
                numericTypes.add(MonetDataType.SMALLINT);
                numericTypes.add(MonetDataType.BIGINT);
                numericTypes.add(MonetDataType.DECIMAL);
                numericTypes.add(MonetDataType.DOUBLE);
                numericTypes.add(MonetDataType.REAL);
            }
            if (!MonetProvider.GENERATE_ONLY_KNOWN && !oS.contains("win")) {
                numericTypes.add(MonetDataType.HUGEINT);
            }
        }

        public static List<MonetDataType> getAllTypes() {
            return new ArrayList<>(allTypes);
        }

        public static MonetDataType getRandomType() {
            return Randomly.fromList(allTypes);
        }

        public static List<MonetDataType> getNumericTypes() {
            return numericTypes;
        }
    }

    public static class MonetColumn extends AbstractTableColumn<MonetTable, MonetDataType> {

        private String tableAlias;

        public MonetColumn(String name, MonetDataType columnType) {
            super(name, null, columnType);
            this.tableAlias = null;
        }

        public MonetColumn(String name, MonetDataType columnType, String tableAlias) {
            super(name, null, columnType);
            this.tableAlias = tableAlias;
        }

        public String getQueryFullQualifiedName() {
            if (this.tableAlias == null) {
                return this.getFullQualifiedName();
            } else {
                return this.tableAlias + "." + this.getName();
            }
        }

        public void setColumnTableAlias(String tableAlias) {
            this.tableAlias = tableAlias;
        }

        public static MonetColumn createDummy(String name) {
            return new MonetColumn(name, MonetDataType.INT);
        }
    }

    public static class MonetTables extends AbstractTables<MonetTable, MonetColumn> {

        public MonetTables(List<MonetTable> tables) {
            super(tables);
        }

        public MonetRowValue getRandomRowValue(SQLConnection con) throws SQLException {
            String randomRow = String.format("SELECT %s FROM %s ORDER BY RAND() LIMIT 1", columnNamesAsString(
                    c -> c.getTable().getName() + "." + c.getName() + " AS " + c.getTable().getName() + c.getName()),
                    // columnNamesAsString(c -> "typeof(" + c.getTable().getName() + "." +
                    // c.getName() + ")")
                    tableNamesAsString());
            Map<MonetColumn, MonetConstant> values = new HashMap<>();
            try (Statement s = con.createStatement()) {
                ResultSet randomRowValues = s.executeQuery(randomRow);
                if (!randomRowValues.next()) {
                    throw new AssertionError("could not find random row! " + randomRow + "\n");
                }
                for (int i = 0; i < getColumns().size(); i++) {
                    MonetColumn column = getColumns().get(i);
                    int columnIndex = randomRowValues.findColumn(column.getTable().getName() + column.getName());
                    assert columnIndex == i + 1;
                    MonetConstant constant;
                    if (randomRowValues.getString(columnIndex) == null) {
                        constant = MonetConstant.createNullConstant();
                    } else {
                        MonetDataType dt = column.getType();
                        switch (dt) {
                        case TINYINT:
                        case SMALLINT:
                        case INT:
                        case BIGINT:
                        case HUGEINT:
                            constant = MonetConstant.createIntConstant(randomRowValues.getLong(columnIndex), dt);
                            break;
                        case BOOLEAN:
                            constant = MonetConstant.createBooleanConstant(randomRowValues.getBoolean(columnIndex));
                            break;
                        case STRING:
                            constant = MonetConstant.createTextConstant(randomRowValues.getString(columnIndex));
                            break;
                        case DECIMAL:
                            constant = MonetConstant.createDecimalConstant(randomRowValues.getBigDecimal(columnIndex));
                            break;
                        case REAL:
                            constant = MonetConstant.createFloatConstant(randomRowValues.getFloat(columnIndex));
                            break;
                        case DOUBLE:
                            constant = MonetConstant.createDoubleConstant(randomRowValues.getDouble(columnIndex));
                            break;
                        case TIME:
                            constant = MonetConstant.createTimeConstant(randomRowValues.getTime(columnIndex).getTime());
                            break;
                        case TIMESTAMP:
                            constant = MonetConstant
                                    .createTimestampConstant(randomRowValues.getTimestamp(columnIndex).getTime());
                            break;
                        case DATE:
                            constant = MonetConstant.createDateConstant(randomRowValues.getDate(columnIndex).getTime());
                            break;
                        case MONTH_INTERVAL:
                            constant = MonetConstant.createMonthIntervalConstant(
                                    randomRowValues.getBigDecimal(columnIndex).longValue());
                            break;
                        case SECOND_INTERVAL:
                        case DAY_INTERVAL:
                            constant = MonetConstant.createSecondIntervalConstant(
                                    randomRowValues.getBigDecimal(columnIndex).longValue(), dt);
                            break;
                        case BLOB:
                            /*
                             * TODO constant = MonetConstant.createBlobConstant(randomRowValues.getBlob(columnIndex) ...
                             * ); break;
                             */
                        case UUID:
                            /*
                             * TODO constant = break;
                             */
                        default:
                            throw new IgnoreMeException();
                        }
                    }
                    values.put(column, constant);
                }
                assert !randomRowValues.next();
                return new MonetRowValue(this, values);
            } catch (SQLException e) {
                throw new IgnoreMeException();
            }
        }

    }

    public static MonetDataType getColumnType(String typeString) {
        switch (typeString) {
        case "tinyint":
            return MonetDataType.TINYINT;
        case "smallint":
            return MonetDataType.SMALLINT;
        case "int":
        case "integer":
            return MonetDataType.INT;
        case "bigint":
            return MonetDataType.BIGINT;
        case "hugeint":
            return MonetDataType.HUGEINT;
        case "boolean":
            return MonetDataType.BOOLEAN;
        case "any": /* we fit nulls as strings */
        case "text":
        case "string":
        case "clob":
        case "char":
        case "varchar":
        case "character":
        case "character varying":
            return MonetDataType.STRING;
        case "numeric":
            return MonetDataType.DECIMAL;
        case "real":
        case "float":
            return MonetDataType.REAL;
        case "double":
        case "double precision":
            return MonetDataType.DOUBLE;
        case "decimal":
            return MonetDataType.DECIMAL;
        case "date":
            return MonetDataType.DATE;
        case "time":
        case "timetz":
            return MonetDataType.TIME;
        case "timestamp":
        case "timestamptz":
            return MonetDataType.TIMESTAMP;
        case "sec_interval":
            return MonetDataType.SECOND_INTERVAL;
        case "day_interval":
            return MonetDataType.DAY_INTERVAL;
        case "month_interval":
            return MonetDataType.MONTH_INTERVAL;
        case "blob":
            return MonetDataType.BLOB;
        case "uuid":
            return MonetDataType.UUID;
        default:
            throw new AssertionError(typeString);
        }
    }

    public static class MonetRowValue extends AbstractRowValue<MonetTables, MonetColumn, MonetConstant> {

        protected MonetRowValue(MonetTables tables, Map<MonetColumn, MonetConstant> values) {
            super(tables, values);
        }

    }

    public static class MonetTable extends AbstractRelationalTable<MonetColumn, MonetIndex, MonetGlobalState> {

        public enum TableType {
            STANDARD, TEMPORARY
        }

        private final TableType tableType;
        private final List<MonetStatisticsObject> statistics;
        private final List<MonetConstraint> constraints;
        private final boolean isInsertable;
        private int columnCounter = 0;
        private int constraintCounter = 0;
        private int indexCounter = 0;

        public MonetTable(String tableName, List<MonetColumn> columns, List<MonetIndex> indexes, TableType tableType,
                List<MonetStatisticsObject> statistics, List<MonetConstraint> constraints, boolean isView, boolean isInsertable) {
            super(tableName, columns, indexes, isView);
            this.statistics = statistics;
            this.constraints = constraints;
            this.isInsertable = isInsertable;
            this.tableType = tableType;
        }

        public List<MonetStatisticsObject> getStatistics() {
            return statistics;
        }

        public List<MonetConstraint> getConstraints() {
            return constraints;
        }

        public MonetConstraint getRandomConstraint() {
            return Randomly.fromList(constraints);
        }

        public TableType getTableType() {
            return tableType;
        }

        public boolean isInsertable() {
            return isInsertable;
        }

        public int getColumnCounter() {
            return columnCounter;
        }

        public int getConstraintCounter() {
            return constraintCounter;
        }

        public int getIndexCounter() {
            return indexCounter;
        }

        public void setColumnCounter(int columnCounter) {
            this.columnCounter = columnCounter;
        }

        public void setConstraintCounter(int constraintCounter) {
            this.constraintCounter = constraintCounter;
        }

        public void setIndexCounter(int indexCounter) {
            this.indexCounter = indexCounter;
        }
    }

    public static final class MonetConstraint { /* for now only keep name */
        private final String name;

        public MonetConstraint(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final class MonetStatisticsObject {
        private final String name;

        public MonetStatisticsObject(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public static final class MonetIndex extends TableIndex {

        private MonetIndex(String indexName) {
            super(indexName);
        }

        public static MonetIndex create(String indexName) {
            return new MonetIndex(indexName);
        }

    }

    public static MonetSchema fromConnection(SQLConnection con, String databaseName) throws SQLException {
        try {
            List<MonetTable> databaseTables = new ArrayList<>();
            try (Statement s = con.createStatement()) {
                try (ResultSet rs = s.executeQuery(
                        "select t.id as table_id, t.name as table_name, t.type as table_type, t.commit_action as table_commit_action from sys.tables t where t.system=false order by table_name;")) {
                    while (rs.next()) {
                        int tableID = rs.getInt("table_id");
                        String tableName = rs.getString("table_name");
                        int tableType = rs.getInt("table_type");
                        int tableCommitAction = rs.getInt("table_commit_action");

                        boolean isView = tableType == 1;
                        MonetTable.TableType monetTableType = getTableType(tableCommitAction);
                        List<MonetColumn> databaseColumns = getTableColumns(con, tableID);
                        List<MonetConstraint> databaseConstraints = getTableConstraints(con, tableID);
                        List<MonetIndex> indexes = getIndexes(con, tableID);
                        List<MonetStatisticsObject> statistics = new ArrayList<>(); // TODO? getStatistics(con);
                        MonetTable t = new MonetTable(tableName, databaseColumns, indexes, monetTableType, statistics, databaseConstraints, isView, !isView);
                        for (MonetColumn c : databaseColumns) {
                            c.setTable(t);
                        }
                        t.setColumnCounter(databaseColumns.size());
                        t.setConstraintCounter(databaseConstraints.size());
                        t.setIndexCounter(indexes.size());
                        databaseTables.add(t);
                    }
                }
            }
            return new MonetSchema(databaseTables, databaseName);
        } catch (SQLIntegrityConstraintViolationException e) {
            throw new AssertionError(e);
        }
    }

    protected static MonetTable.TableType getTableType(int tableCommitAction) throws AssertionError {
        MonetTable.TableType tableType;
        if (tableCommitAction == 0) {
            tableType = TableType.STANDARD;
        } else if (tableCommitAction > 0) {
            tableType = TableType.TEMPORARY;
        } else {
            throw new AssertionError("Unkwown commit action");
        }
        return tableType;
    }

    protected static List<MonetIndex> getIndexes(SQLConnection con, int tableID) throws SQLException {
        List<MonetIndex> indexes = new ArrayList<>();
        try (Statement s = con.createStatement()) {
            try (ResultSet rs = s.executeQuery(String.format(
                    "SELECT idxs.\"name\" as indexname FROM sys.idxs WHERE table_id='%d' order by indexname;",
                    tableID))) {
                while (rs.next()) {
                    String indexName = rs.getString("indexname");
                    indexes.add(MonetIndex.create(indexName));
                }
            }
        }
        return indexes;
    }

    protected static List<MonetColumn> getTableColumns(SQLConnection con, int tableID) throws SQLException {
        List<MonetColumn> columns = new ArrayList<>();
        try (Statement s = con.createStatement()) {
            try (ResultSet rs = s.executeQuery(
                    "select columns.\"name\" as column_name, columns.\"type\" as data_type from sys.columns where table_id = '"
                            + tableID + "' order by column_name")) {
                while (rs.next()) {
                    String columnName = rs.getString("column_name");
                    String dataType = rs.getString("data_type");
                    MonetColumn c = new MonetColumn(columnName, getColumnType(dataType));
                    columns.add(c);
                }
            }
        }
        return columns;
    }

    protected static List<MonetConstraint> getTableConstraints(SQLConnection con, int tableID) throws SQLException {
        List<MonetConstraint> constraints = new ArrayList<>();
        try (Statement s = con.createStatement()) {
            try (ResultSet rs = s.executeQuery(
                    "select keys.\"name\" as key_name from sys.keys where table_id = '"
                            + tableID + "' order by key_name")) {
                while (rs.next()) {
                    String constraintName = rs.getString("key_name");
                    MonetConstraint c = new MonetConstraint(constraintName);
                    constraints.add(c);
                }
            }
        }
        return constraints;
    }

    public MonetSchema(List<MonetTable> databaseTables, String databaseName) {
        super(databaseTables);
        this.databaseName = databaseName;
    }

    public MonetTables getRandomTableNonEmptyTables() {
        List<MonetTable> tables = getDatabaseTables();
        if (tables.isEmpty()) {
            return new MonetTables(Collections.emptyList());
        } else {
            return new MonetTables(
                    Randomly.nonEmptySubset(tables, Math.min(tables.size(), Randomly.fromOptions(1, 2, 3))));
        }
    }

    public List<MonetTable> getRandomTableNonEmptyTablesAsList() {
        List<MonetTable> tables = getDatabaseTables();
        if (tables.isEmpty()) {
            return Collections.emptyList();
        } else {
            return Randomly.nonEmptySubset(tables, Math.min(tables.size(), Randomly.fromOptions(1, 2, 3)));
        }
    }

    public String getDatabaseName() {
        return databaseName;
    }

}
