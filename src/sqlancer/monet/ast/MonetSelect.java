package sqlancer.monet.ast;

import java.util.Collections;
import java.util.List;

import sqlancer.Randomly;
import sqlancer.ast.SelectBase;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;

public class MonetSelect extends SelectBase<MonetExpression> implements MonetExpression {

    private SelectType selectOption = SelectType.ALL;
    private List<MonetJoin> joinClauses = Collections.emptyList();
    private MonetExpression distinctOnClause;

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

    public void setDistinctOnClause(MonetExpression distinctOnClause) {
        if (selectOption != SelectType.DISTINCT) {
            throw new IllegalArgumentException();
        }
        this.distinctOnClause = distinctOnClause;
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

    public MonetExpression getDistinctOnClause() {
        return distinctOnClause;
    }
}
