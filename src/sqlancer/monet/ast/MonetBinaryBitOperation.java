package sqlancer.monet.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetBinaryBitOperation.MonetBinaryBitOperator;

public class MonetBinaryBitOperation extends BinaryOperatorNode<MonetExpression, MonetBinaryBitOperator>
        implements MonetExpression {

    public enum MonetBinaryBitOperator implements Operator {
        CONCATENATION("||"), //
        BITWISE_AND("&"), //
        BITWISE_OR("|"), //
        BITWISE_XOR("^"), //
        BITWISE_SHIFT_LEFT("<<"), //
        BITWISE_SHIFT_RIGHT(">>");

        private String text;

        MonetBinaryBitOperator(String text) {
            this.text = text;
        }

        public static MonetBinaryBitOperator getRandom() {
            return Randomly.fromOptions(MonetBinaryBitOperator.values());
        }

        @Override
        public String getTextRepresentation() {
            return text;
        }

    }

    public MonetBinaryBitOperation(MonetBinaryBitOperator op, MonetExpression left, MonetExpression right) {
        super(left, right, op);
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }

}
