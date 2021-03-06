package sqlancer.monet.ast;

import java.util.List;

import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetInOperation implements MonetExpression {

    private final MonetExpression expr;
    private final List<MonetExpression> listElements;
    private final boolean isTrue;

    public MonetInOperation(MonetExpression expr, List<MonetExpression> listElements, boolean isTrue) {
        this.expr = expr;
        this.listElements = listElements;
        this.isTrue = isTrue;
    }

    public MonetExpression getExpr() {
        return expr;
    }

    public List<MonetExpression> getListElements() {
        return listElements;
    }

    @Override
    public MonetConstant getExpectedValue() {
        MonetConstant leftValue = expr.getExpectedValue();
        if (leftValue == null) {
            return null;
        }
        if (leftValue.isNull()) {
            return MonetConstant.createNullConstant();
        }
        boolean isNull = false;
        for (MonetExpression expr : getListElements()) {
            MonetConstant rightExpectedValue = expr.getExpectedValue();
            if (rightExpectedValue == null) {
                return null;
            }
            if (rightExpectedValue.isNull()) {
                isNull = true;
            } else if (rightExpectedValue.isEquals(this.expr.getExpectedValue()).isBoolean()
                    && rightExpectedValue.isEquals(this.expr.getExpectedValue()).asBoolean()) {
                return MonetConstant.createBooleanConstant(isTrue);
            }
        }

        if (isNull) {
            return MonetConstant.createNullConstant();
        } else {
            return MonetConstant.createBooleanConstant(!isTrue);
        }
    }

    public boolean isTrue() {
        return isTrue;
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }
}
