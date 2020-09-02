package sqlancer.monet.ast;

import sqlancer.monet.ast.MonetBinaryComparisonOperation.MonetBinaryComparisonOperator;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetAnyAllOperation implements MonetExpression {

    private final MonetExpression expr;
    private final MonetBinaryComparisonOperator comparison;
    private final boolean isAny;
    private final MonetSelect select;

    public MonetAnyAllOperation(MonetExpression expr, MonetBinaryComparisonOperator comparison, boolean isAny, MonetSelect select) {
        this.expr = expr;
        this.select = select;
        this.isAny = isAny; /* ANY vs ALL */
        this.comparison = comparison;
    }

    public MonetExpression getExpr() {
        return expr;
    }

    public MonetSelect getSelect() {
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
