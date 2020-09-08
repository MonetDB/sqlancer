package sqlancer.monet.ast;

import java.util.Collections;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;

public class MonetSelect extends MonetQuery {

    private SelectType selectOption = SelectType.ALL;
    private List<MonetJoin> joinClauses = Collections.emptyList();

    public static class MonetFromTable implements MonetExpression {
        private final MonetTable t;
        private final boolean only;

        public MonetFromTable(MonetTable t, boolean only) {
            this.t = t;
            this.only = only;
        }

        public MonetTable getTable() {
            return t;
        }

        public boolean isOnly() {
            return only;
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
