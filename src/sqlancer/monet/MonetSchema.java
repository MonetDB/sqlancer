package sqlancer.monet;

import java.sql.Connection;
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
import java.util.function.Function;
import java.util.stream.Collectors;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.StateToReproduce.MonetStateToReproduce;
import sqlancer.monet.MonetSchema.MonetTable.TableType;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.schema.AbstractTable;
import sqlancer.schema.AbstractTableColumn;
import sqlancer.schema.AbstractTables;
import sqlancer.schema.TableIndex;

public class MonetSchema {

    private final List<MonetTable> databaseTables;
    private final String databaseName;

    public enum MonetDataType {
        INT, BOOLEAN, STRING, DECIMAL, REAL, DOUBLE, TIME, TIMESTAMP, DATE, MONTH_INTERVAL, SECOND_INTERVAL;

        public static MonetDataType getRandomType() {
            List<MonetDataType> dataTypes = Arrays.asList(values());
            /*if (MonetProvider.generateOnlyKnown) {
                dataTypes.remove(MonetDataType.DECIMAL);
                dataTypes.remove(MonetDataType.FLOAT);
                dataTypes.remove(MonetDataType.REAL);
            }*/
            return Randomly.fromList(dataTypes);
        }
    }

    public static class MonetColumn extends AbstractTableColumn<MonetTable, MonetDataType> {

        public MonetColumn(String name, MonetDataType columnType) {
            super(name, null, columnType);
        }

    }

    public static class MonetTables extends AbstractTables<MonetTable, MonetColumn> {

        public MonetTables(List<MonetTable> tables) {
            super(tables);
        }

        public MonetRowValue getRandomRowValue(Connection con, MonetStateToReproduce state) throws SQLException {
            String randomRow = String.format("SELECT %s FROM %s ORDER BY RAND() LIMIT 1", columnNamesAsString(
                    c -> c.getTable().getName() + "." + c.getName() + " AS " + c.getTable().getName() + c.getName()),
                    // columnNamesAsString(c -> "typeof(" + c.getTable().getName() + "." +
                    // c.getName() + ")")
                    tableNamesAsString());
            Map<MonetColumn, MonetConstant> values = new HashMap<>();
            try (Statement s = con.createStatement()) {
                ResultSet randomRowValues = s.executeQuery(randomRow);
                if (!randomRowValues.next()) {
                    throw new AssertionError("could not find random row! " + randomRow + "\n" + state);
                }
                for (int i = 0; i < getColumns().size(); i++) {
                    MonetColumn column = getColumns().get(i);
                    int columnIndex = randomRowValues.findColumn(column.getTable().getName() + column.getName());
                    assert columnIndex == i + 1;
                    MonetConstant constant;
                    if (randomRowValues.getString(columnIndex) == null) {
                        constant = MonetConstant.createNullConstant();
                    } else {
                        switch (column.getType()) {
                        case INT:
                            constant = MonetConstant.createIntConstant(randomRowValues.getLong(columnIndex));
                            break;
                        case BOOLEAN:
                            constant = MonetConstant.createBooleanConstant(randomRowValues.getBoolean(columnIndex));
                            break;
                        case STRING:
                            constant = MonetConstant.createTextConstant(randomRowValues.getString(columnIndex));
                            break;
                        default:
                            throw new AssertionError(column.getType());
                        }
                    }
                    values.put(column, constant);
                }
                assert (!randomRowValues.next());
                state.randomRowValues = values;
                return new MonetRowValue(this, values);
            }
        }

    }

    private static MonetDataType getColumnType(String typeString) {
        switch (typeString) {
        case "tinyint":
        case "int":
        case "smallint":
        case "integer":
        case "bigint":
        case "hugeint":
            return MonetDataType.INT;
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
        case "month_interval":
            return MonetDataType.MONTH_INTERVAL;
        case "sec_interval":
            return MonetDataType.SECOND_INTERVAL;
        default:
            throw new AssertionError(typeString);
        }
    }

    public static class MonetRowValue {

        private final MonetTables tables;
        private final Map<MonetColumn, MonetConstant> values;

        MonetRowValue(MonetTables tables, Map<MonetColumn, MonetConstant> values) {
            this.tables = tables;
            this.values = values;
        }

        public MonetTables getTable() {
            return tables;
        }

        public Map<MonetColumn, MonetConstant> getValues() {
            return values;
        }

        @Override
        public String toString() {
            StringBuffer sb = new StringBuffer();
            int i = 0;
            for (MonetColumn c : tables.getColumns()) {
                if (i++ != 0) {
                    sb.append(", ");
                }
                sb.append(values.get(c));
            }
            return sb.toString();
        }

        public String getRowValuesAsString() {
            List<MonetColumn> columnsToCheck = tables.getColumns();
            return getRowValuesAsString(columnsToCheck);
        }

        public String getRowValuesAsString(List<MonetColumn> columnsToCheck) {
            StringBuilder sb = new StringBuilder();
            Map<MonetColumn, MonetConstant> expectedValues = getValues();
            for (int i = 0; i < columnsToCheck.size(); i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                MonetConstant expectedColumnValue = expectedValues.get(columnsToCheck.get(i));
                MonetToStringVisitor visitor = new MonetToStringVisitor();
                visitor.visit(expectedColumnValue);
                sb.append(visitor.get());
            }
            return sb.toString();
        }

    }

    public static class MonetTable extends AbstractTable<MonetColumn, MonetIndex> {

        public enum TableType {
            STANDARD, TEMPORARY
        }

        private final TableType tableType;
        private final List<MonetStatisticsObject> statistics;
        private final boolean isInsertable;

        public MonetTable(String tableName, List<MonetColumn> columns, List<MonetIndex> indexes,
                TableType tableType, List<MonetStatisticsObject> statistics, boolean isView, boolean isInsertable) {
            super(tableName, columns, indexes, isView);
            this.statistics = statistics;
            this.isInsertable = isInsertable;
            this.tableType = tableType;
        }

        public List<MonetStatisticsObject> getStatistics() {
            return statistics;
        }

        public TableType getTableType() {
            return tableType;
        }

        public boolean isInsertable() {
            return isInsertable;
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

        @Override
        public String getIndexName() {
            return super.getIndexName();
        }

    }

    public static MonetSchema fromConnection(Connection con, String databaseName) throws SQLException {
        Exception ex = null;
        try {
            List<MonetTable> databaseTables = new ArrayList<>();
            try (Statement s = con.createStatement()) {
                try (ResultSet rs = s.executeQuery(
                    "select t.id as table_id, t.name as table_name, t.type as table_type, t.commit_action as table_commit_action from sys.tables t where t.system=false;")) {
                    while (rs.next()) {
                        int tableID = rs.getInt("table_id");
                        String tableName = rs.getString("table_name");
                        int tableType = rs.getInt("table_type");
                        int tableCommitAction = rs.getInt("table_commit_action");

                        boolean isView = tableType == 1;
                        MonetTable.TableType MonetTableType = getTableType(tableCommitAction);
                        List<MonetColumn> databaseColumns = getTableColumns(con, tableID);
                        List<MonetIndex> indexes = getIndexes(con, tableID);
                        List<MonetStatisticsObject> statistics = new ArrayList<>(); //TODO? getStatistics(con);
                        MonetTable t = new MonetTable(tableName, databaseColumns, indexes, MonetTableType, statistics, isView, true);
                        for (MonetColumn c : databaseColumns) {
                            c.setTable(t);
                        }
                        databaseTables.add(t);
                    }
                }
            }
            return new MonetSchema(databaseTables, databaseName);
        } catch (SQLIntegrityConstraintViolationException e) {
            ex = e;
        }
        throw new AssertionError(ex);
    }

    private static MonetTable.TableType getTableType(int tableCommitAction) throws AssertionError {
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

    private static List<MonetIndex> getIndexes(Connection con, int tableID) throws SQLException {
        List<MonetIndex> indexes = new ArrayList<>();
        try (Statement s = con.createStatement()) {
            try (ResultSet rs = s
                    .executeQuery(String.format("SELECT idxs.\"name\" as indexname FROM sys.idxs WHERE table_id='%d';", tableID))) {
                while (rs.next()) {
                    String indexName = rs.getString("indexname");
                    indexes.add(MonetIndex.create(indexName));
                }
            }
        }
        return indexes;
    }

    private static List<MonetColumn> getTableColumns(Connection con, int tableID) throws SQLException {
        List<MonetColumn> columns = new ArrayList<>();
        try (Statement s = con.createStatement()) {
            try (ResultSet rs = s
                    .executeQuery("select columns.\"name\" as column_name, columns.\"type\" as data_type from sys.columns where table_id = '"
                            + tableID + "'")) {
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

    public MonetSchema(List<MonetTable> databaseTables, String databaseName) {
        this.databaseTables = Collections.unmodifiableList(databaseTables);
        this.databaseName = databaseName;
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        for (MonetTable t : getDatabaseTables()) {
            sb.append(t + "\n");
        }
        return sb.toString();
    }

    public MonetTable getRandomTable() {
        return Randomly.fromList(getDatabaseTables());
    }

    public MonetTables getRandomTableNonEmptyTables() {
        return new MonetTables(Randomly.nonEmptySubset(databaseTables));
    }

    public List<MonetTable> getDatabaseTables() {
        return databaseTables;
    }

    public List<MonetTable> getDatabaseTablesRandomSubsetNotEmpty() {
        return Randomly.nonEmptySubset(databaseTables);
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public MonetTable getRandomTable(Function<MonetTable, Boolean> f) {
        List<MonetTable> relevantTables = databaseTables.stream().filter(t -> f.apply(t))
                .collect(Collectors.toList());
        if (relevantTables.isEmpty()) {
            throw new IgnoreMeException();
        }
        return Randomly.fromList(relevantTables);
    }

}
