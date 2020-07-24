package sqlancer.monet.ast;

import sqlancer.Randomly;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetJoin implements MonetExpression {

    public enum MonetJoinType {
        INNER, LEFT, RIGHT, FULL, CROSS, NATURAL;

        public static MonetJoinType getRandom() {
            return Randomly.fromOptions(values());
        }

    }

    private final MonetExpression tableReference;
    private final MonetExpression onClause;
    private final MonetJoinType type;

    public MonetJoin(MonetExpression tableReference, MonetExpression onClause, MonetJoinType type) {
        this.tableReference = tableReference;
        this.onClause = onClause;
        this.type = type;
    }

    public MonetExpression getTableReference() {
        return tableReference;
    }

    public MonetExpression getOnClause() {
        return onClause;
    }

    public MonetJoinType getType() {
        return type;
    }

    @Override
    public MonetDataType getExpressionType() {
        throw new AssertionError();
    }

    @Override
    public MonetConstant getExpectedValue() {
        throw new AssertionError();
    }

}
