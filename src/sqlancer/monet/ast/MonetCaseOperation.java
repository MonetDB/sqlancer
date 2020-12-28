package sqlancer.monet.ast;

import java.util.List;

import sqlancer.monet.MonetSchema.MonetDataType;

public final class MonetCaseOperation implements MonetExpression {

    private final List<MonetExpression> conditions;
    private final List<MonetExpression> expressions;
    private final MonetExpression elseExpr;
    private final MonetExpression switchCondition;
    private final MonetDataType type;

    public MonetCaseOperation(MonetExpression switchCondition, List<MonetExpression> conditions,
            List<MonetExpression> expressions, MonetExpression elseExpr, MonetDataType type) {
        this.switchCondition = switchCondition;
        this.conditions = conditions;
        this.expressions = expressions;
        this.elseExpr = elseExpr;
        this.type = type;
        if (conditions.size() != expressions.size()) {
            throw new IllegalArgumentException();
        }
    }

    public MonetExpression getSwitchCondition() {
        return switchCondition;
    }

    public List<MonetExpression> getConditions() {
        return conditions;
    }

    public List<MonetExpression> getExpressions() {
        return expressions;
    }

    public MonetExpression getElseExpr() {
        return elseExpr;
    }

    @Override
    public MonetConstant getExpectedValue() {
        throw new AssertionError(this);
    }

    @Override
    public MonetDataType getExpressionType() {
        return type;
    }

}
