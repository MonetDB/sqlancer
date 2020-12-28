package sqlancer.monet.ast;

import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetBinaryComparisonOperation.MonetBinaryComparisonOperator;

public class MonetAnyAllOperation implements MonetExpression {

    private final MonetExpression expr;
    private final MonetBinaryComparisonOperator comparison;
    private final boolean isAny;
    private final MonetQuery select;

    public MonetAnyAllOperation(MonetExpression expr, MonetBinaryComparisonOperator comparison, boolean isAny,
            MonetQuery select) {
        this.expr = expr;
        this.select = select;
        this.isAny = isAny; /* ANY vs ALL */
        this.comparison = comparison;
    }

    public MonetExpression getExpr() {
        return expr;
    }

    public MonetQuery getSelect() {
        return select;
    }

    public MonetBinaryComparisonOperator getComparison() {
        return comparison;
    }

    @Override
    public MonetConstant getExpectedValue() {
        throw new AssertionError(this);
    }

    public boolean isAny() {
        return isAny;
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }
}
