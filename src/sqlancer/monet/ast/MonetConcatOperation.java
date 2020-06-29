package sqlancer.monet.ast;

import sqlancer.ast.BinaryNode;
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
        if (getLeft().getExpectedValue().isNull() || getRight().getExpectedValue().isNull()) {
            return MonetConstant.createNullConstant();
        }
        String leftStr = getLeft().getExpectedValue().cast(MonetDataType.STRING).getUnquotedTextRepresentation();
        String rightStr = getRight().getExpectedValue().cast(MonetDataType.STRING).getUnquotedTextRepresentation();
        return MonetConstant.createTextConstant(leftStr + rightStr);
    }

    @Override
    public String getOperatorRepresentation() {
        return "||";
    }

}
