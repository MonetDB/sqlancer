package sqlancer.monet.ast;

import sqlancer.monet.MonetSchema.MonetDataType;

public interface MonetExpression {

    default MonetDataType getExpressionType() {
        throw new AssertionError("operator does not support PQS evaluation!");
    }

    default MonetConstant getExpectedValue() {
        throw new AssertionError("operator does not support PQS evaluation!");
    }
}
