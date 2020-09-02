package sqlancer.monet.ast;

import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetColumnValue implements MonetExpression {

    private final MonetColumn c;
    private final MonetConstant expectedValue;

    public MonetColumnValue(MonetColumn c, MonetConstant expectedValue) {
        this.c = c;
        this.expectedValue = expectedValue;
    }

    @Override
    public MonetDataType getExpressionType() {
        return c.getType();
    }

    @Override
    public MonetConstant getExpectedValue() {
        return expectedValue;
    }

    public static MonetColumnValue create(MonetColumn c, MonetConstant expected) {
        return new MonetColumnValue(c, expected);
    }

    public MonetColumn getColumn() {
        return c;
    }

}
