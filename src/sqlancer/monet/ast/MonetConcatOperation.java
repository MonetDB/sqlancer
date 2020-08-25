package sqlancer.monet.ast;

import sqlancer.common.ast.BinaryNode;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetConcatOperation extends BinaryNode<MonetExpression> implements MonetExpression {

    public MonetConcatOperation(MonetExpression left, MonetExpression right) {
        super(left, right);
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.STRING;
    }

    @Override
    public MonetConstant getExpectedValue() {
        MonetConstant leftExpectedValue = getLeft().getExpectedValue();
        MonetConstant rightExpectedValue = getRight().getExpectedValue();
        if (leftExpectedValue == null || rightExpectedValue == null) {
            return null;
        }
        if (leftExpectedValue.isNull() || rightExpectedValue.isNull()) {
            return MonetConstant.createNullConstant();
        }
        String leftStr = leftExpectedValue.cast(MonetDataType.STRING).getUnquotedTextRepresentation();
        String rightStr = rightExpectedValue.cast(MonetDataType.STRING).getUnquotedTextRepresentation();
        return MonetConstant.createTextConstant(leftStr + rightStr);
    }

    @Override
    public String getOperatorRepresentation() {
        return "||";
    }

}
