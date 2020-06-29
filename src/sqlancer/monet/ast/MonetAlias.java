package sqlancer.monet.ast;

import sqlancer.visitor.UnaryOperation;

public class MonetAlias implements UnaryOperation<MonetExpression>, MonetExpression {

    private MonetExpression expr;
    private String alias;

    public MonetAlias(MonetExpression expr, String alias) {
        this.expr = expr;
        this.alias = alias;
    }

    @Override
    public MonetExpression getExpression() {
        return expr;
    }

    @Override
    public String getOperatorRepresentation() {
        return " as " + alias;
    }

    @Override
    public OperatorKind getOperatorKind() {
        return OperatorKind.POSTFIX;
    }

    @Override
    public boolean omitBracketsWhenPrinting() {
        return true;
    }

}
