package sqlancer.monet.ast;

import sqlancer.LikeImplementationHelper;
import sqlancer.ast.BinaryNode;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetLikeOperation extends BinaryNode<MonetExpression> implements MonetExpression {

    private final boolean caseIsensitive;
    private final boolean isNot;

    public MonetLikeOperation(MonetExpression left, MonetExpression right, boolean caseIsensitive, boolean isNot) {
        super(left, right);
        this.caseIsensitive = caseIsensitive;
        this.isNot = isNot;
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }

    @Override
    public MonetConstant getExpectedValue() {
        MonetConstant leftVal = getLeft().getExpectedValue();
        MonetConstant rightVal = getRight().getExpectedValue();
        if (leftVal.isNull() || rightVal.isNull()) {
            return MonetConstant.createNullConstant();
        } else {
            boolean val = LikeImplementationHelper.match(leftVal.asString(), rightVal.asString(), 0, 0, true);
            return MonetConstant.createBooleanConstant(val);
        }
    }

    @Override
    public String getOperatorRepresentation() {
        if (caseIsensitive && isNot) {
            return "NOT ILIKE";
        } else if(!caseIsensitive && isNot) {
            return "NOT LIKE";
        } else if (caseIsensitive && !isNot) {
            return "ILIKE";
        } else {
            return "LIKE";
        }
    }

}
