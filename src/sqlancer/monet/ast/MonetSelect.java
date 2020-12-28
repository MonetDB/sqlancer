package sqlancer.monet.ast;

import java.util.Collections;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;

public class MonetSelect extends MonetQuery {

    private SelectType selectOption = SelectType.ALL;
    private List<MonetJoin> joinClauses = Collections.emptyList();

    public static class MonetFromTable implements MonetExpression {
        private final MonetTable t;
        private final String tableAlias;
        private final boolean only;

        public MonetFromTable(MonetTable t, boolean only, String tableAlias) {
            this.t = t;
            this.only = only;
            this.tableAlias = tableAlias;
        }

        public MonetTable getTable() {
            return t;
        }

        public boolean isOnly() {
            return only;
        }

        public String getTableAlias() {
            return tableAlias;
        }

        @Override
        public MonetDataType getExpressionType() {
            return null;
        }
    }

    public static class MonetCTE extends MonetTable {
        private final MonetQuery query;

        public MonetCTE(String tableName, List<MonetColumn> columns, MonetQuery query) {
            super(tableName, columns, Collections.emptyList(), TableType.TEMPORARY, Collections.emptyList(), false,
                    false);
            this.query = query;
        }

        public MonetQuery getQuery() {
            return query;
        }
    }

    public static class MonetQueryCTE implements MonetExpression {
        private final MonetCTE cte;
        private final String name;
        private final String tableAlias;

        public MonetQueryCTE(MonetCTE cte, String name, String tableAlias) {
            this.cte = cte;
            this.name = name;
            this.tableAlias = tableAlias;
        }

        public MonetCTE getCTE() {
            return cte;
        }

        public String getName() {
            return name;
        }

        public String getTableAlias() {
            return tableAlias;
        }

        @Override
        public MonetDataType getExpressionType() {
            return null;
        }
    }

    public enum SelectType {
        DISTINCT, ALL;

        public static SelectType getRandom() {
            return Randomly.fromOptions(values());
        }
    }

    public void setSelectType(SelectType fromOptions) {
        this.setSelectOption(fromOptions);
    }

    public SelectType getSelectOption() {
        return selectOption;
    }

    public void setSelectOption(SelectType fromOptions) {
        this.selectOption = fromOptions;
    }

    @Override
    public MonetDataType getExpressionType() {
        return null;
    }

    public void setJoinClauses(List<MonetJoin> joinStatements) {
        this.joinClauses = joinStatements;

    }

    public List<MonetJoin> getJoinClauses() {
        return joinClauses;
    }

}
