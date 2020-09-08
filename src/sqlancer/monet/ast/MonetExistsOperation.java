package sqlancer.monet.ast;

import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetExistsOperation implements MonetExpression {

    private final MonetQuery select;
    private final boolean isExists;

    public MonetExistsOperation(MonetQuery select, boolean isExists) {
        this.select = select;
        this.isExists = isExists; /* NOT EXISTS vs EXISTS */
    }

    public MonetQuery getSelect() {
        return select;
    }

    @Override
    public MonetConstant getExpectedValue() {
        throw new AssertionError(this);
    }

    public boolean isExists() {
        return isExists;
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }
}
