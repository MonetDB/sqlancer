package sqlancer.monet.ast;

import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetBinaryComparisonOperation.MonetBinaryComparisonOperator;
import sqlancer.monet.ast.MonetBinaryLogicalOperation.BinaryLogicalOperator;

public final class MonetBetweenOperation implements MonetExpression {

    private final MonetExpression expr;
    private final MonetExpression left;
    private final MonetExpression right;
    private final boolean isSymmetric;
    private final boolean isNot;

    public MonetBetweenOperation(MonetExpression expr, MonetExpression left, MonetExpression right, boolean symmetric,
            boolean isNot) {
        this.expr = expr;
        this.left = left;
        this.right = right;
        this.isSymmetric = symmetric;
        this.isNot = isNot;
    }

    public MonetExpression getExpr() {
        return expr;
    }

    public MonetExpression getLeft() {
        return left;
    }

    public MonetExpression getRight() {
        return right;
    }

    public boolean isSymmetric() {
        return isSymmetric;
    }

    public boolean isNot() {
        return isNot;
    }

    @Override
    public MonetConstant getExpectedValue() {
        MonetBinaryComparisonOperation leftComparison = new MonetBinaryComparisonOperation(left, expr,
                MonetBinaryComparisonOperator.LESS_EQUALS);
        MonetBinaryComparisonOperation rightComparison = new MonetBinaryComparisonOperation(expr, right,
                MonetBinaryComparisonOperator.LESS_EQUALS);
        MonetBinaryLogicalOperation andOperation = new MonetBinaryLogicalOperation(leftComparison, rightComparison,
                MonetBinaryLogicalOperation.BinaryLogicalOperator.AND);
        if (isSymmetric) {
            MonetBinaryComparisonOperation leftComparison2 = new MonetBinaryComparisonOperation(right, expr,
                    MonetBinaryComparisonOperator.LESS_EQUALS);
            MonetBinaryComparisonOperation rightComparison2 = new MonetBinaryComparisonOperation(expr, left,
                    MonetBinaryComparisonOperator.LESS_EQUALS);
            MonetBinaryLogicalOperation andOperation2 = new MonetBinaryLogicalOperation(leftComparison2,
                    rightComparison2, MonetBinaryLogicalOperation.BinaryLogicalOperator.AND);
            MonetBinaryLogicalOperation orOp = new MonetBinaryLogicalOperation(andOperation, andOperation2,
                    BinaryLogicalOperator.OR);
            return orOp.getExpectedValue();
        } else {
            return andOperation.getExpectedValue();
        }
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }

}
