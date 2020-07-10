package sqlancer.monet.ast;

import sqlancer.monet.MonetCompoundDataType;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetCastOperation implements MonetExpression {

    private final MonetExpression expression;
    private final MonetCompoundDataType type;

    public MonetCastOperation(MonetExpression expression, MonetCompoundDataType type) {
        if (expression == null) {
            throw new AssertionError();
        }
        this.expression = expression;
        this.type = type;
    }

    @Override
    public MonetDataType getExpressionType() {
        return type.getDataType();
    }

    @Override
    public MonetConstant getExpectedValue() {
        return expression.getExpectedValue().cast(type.getDataType());
    }

    public MonetExpression getExpression() {
        return expression;
    }

    public MonetDataType getType() {
        return type.getDataType();
    }

    public MonetCompoundDataType getCompoundType() {
        return type;
    }

}
