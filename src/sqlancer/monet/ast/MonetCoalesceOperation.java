package sqlancer.monet.ast;

import java.util.List;

import sqlancer.monet.MonetSchema.MonetDataType;

public final class MonetCoalesceOperation implements MonetExpression {

    private final List<MonetExpression> conditions;

    public MonetCoalesceOperation(List<MonetExpression> conditions) {
        this.conditions = conditions;
    }

    public List<MonetExpression> getConditions() {
        return conditions;
    }

    @Override
    public MonetConstant getExpectedValue() {
        throw new AssertionError(this);
    }

    @Override
    public MonetDataType getExpressionType() {
        return null;
    }

}
