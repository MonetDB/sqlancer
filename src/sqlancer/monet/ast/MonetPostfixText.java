package sqlancer.monet.ast;

import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetPostfixText implements MonetExpression {

    private final MonetExpression expr;
    private final String text;
    private final MonetConstant expectedValue;
    private final MonetDataType type;

    public MonetPostfixText(MonetExpression expr, String text, MonetConstant expectedValue,
            MonetDataType type) {
        this.expr = expr;
        this.text = text;
        this.expectedValue = expectedValue;
        this.type = type;
    }

    public MonetExpression getExpr() {
        return expr;
    }

    public String getText() {
        return text;
    }

    @Override
    public MonetConstant getExpectedValue() {
        return expectedValue;
    }

    @Override
    public MonetDataType getExpressionType() {
        return type;
    }
}
