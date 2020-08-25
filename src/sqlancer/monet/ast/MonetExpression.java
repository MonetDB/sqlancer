package sqlancer.monet.ast;

import sqlancer.monet.MonetSchema.MonetDataType;

public interface MonetExpression {

    default MonetDataType getExpressionType() {
        return null;
    }

    default MonetConstant getExpectedValue() {
        return null;
    }
}
