package sqlancer.monet.gen;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import sqlancer.GlobalState;
import sqlancer.Randomly;
import sqlancer.SQLConnection;
import sqlancer.common.query.SQLQueryAdapter;

public final class MonetQueryCatalogGenerator {

    private MonetQueryCatalogGenerator() {
    }

    public static SQLQueryAdapter query() {
        StringBuilder sb = new StringBuilder();
        sb.append("SELECT * FROM ");
        sb.append(Randomly.fromOptions("schemas", "types", "functions", "args", "sequences", "table_partitions",
                "range_partitions", "value_partitions", "dependencies", "_tables", "_columns", "keys", "idxs",
                "triggers", "objects", "_tables", "_columns", "keys", "idxs", "triggers", "objects", "tables",
                "columns", "comments", "db_user_info", "users", "user_role", "auths", "privileges", "querylog_catalog",
                "querylog_calls", "querylog_history", "tracelog", "ids", "dependency_types", "dependencies_vw",
                "dependency_owners_on_schemas", "dependency_columns_on_keys", "dependency_tables_on_views",
                "dependency_views_on_views", "dependency_columns_on_views", "dependency_functions_on_views",
                "dependency_schemas_on_users", "dependency_tables_on_functions", "dependency_views_on_functions",
                "dependency_columns_on_functions", "dependency_functions_on_functions", "dependency_tables_on_triggers",
                "dependency_columns_on_triggers", "dependency_functions_on_triggers", "dependency_tables_on_indexes",
                "dependency_columns_on_indexes", "dependency_tables_on_foreignkeys", "dependency_keys_on_foreignkeys",
                "dependency_tables_on_procedures", "dependency_views_on_procedures", "dependency_columns_on_procedures",
                "dependency_functions_on_procedures", "dependency_columns_on_types", "dependency_functions_on_types",
                "dependency_args_on_types", "sessions", "prepared_statements", "prepared_statements_args", "optimizers",
                "environment", "queue", "rejects", "spatial_ref_sys", "geometry_columns", "keywords", "table_types",
                "function_types", "function_languages", "key_types", "index_types", "privilege_codes", "roles",
                "var_values", "storage", "tablestorage", "schemastorage", "storagemodelinput", "statistics",
                "systemfunctions"));
        return new SQLQueryAdapter(sb.toString()) {
            @Override
            public <G extends GlobalState<?, ?, SQLConnection>> boolean execute(G globalState, String... fills)
                    throws SQLException {
                try (Statement s = globalState.getConnection().createStatement()) {
                    try (ResultSet rs = s.executeQuery(getQueryString())) {
                        // CHECKSTYLE:OFF
                        while (rs.next()) {
                            // only force the DBMS to fetch the records and hope that they cause an
                            // invalid state
                        }
                        // CHECKSTYLE:ON
                    }
                }
                return true;
            }
        };
    }

}
