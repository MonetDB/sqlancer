package sqlancer.monet.ast;

import java.util.function.BinaryOperator;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetBinaryArithmeticOperation.MonetBinaryOperator;

public class MonetBinaryArithmeticOperation extends BinaryOperatorNode<MonetExpression, MonetBinaryOperator>
        implements MonetExpression {

    public enum MonetBinaryOperator implements Operator {

        ADDITION("+") {
            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                return applyBitOperation(left, right, (l, r) -> l + r);
            }

        },
        SUBTRACTION("-") {
            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                return applyBitOperation(left, right, (l, r) -> l - r);
            }
        },
        MULTIPLICATION("*") {
            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                return applyBitOperation(left, right, (l, r) -> l * r);
            }
        },
        DIVISION("/") {

            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                return applyBitOperation(left, right, (l, r) -> r == 0 ? -1 : l / r);

            }

        },
        MODULO("%") {
            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                return applyBitOperation(left, right, (l, r) -> r == 0 ? -1 : l % r);

            }
        };

        private String textRepresentation;

        private static MonetConstant applyBitOperation(MonetConstant left, MonetConstant right,
                BinaryOperator<Long> op) {
            if (left.isNull() || right.isNull()) {
                return MonetConstant.createNullConstant();
            } else {
                long leftVal = left.cast(MonetDataType.INT).asInt();
                long rightVal = right.cast(MonetDataType.INT).asInt();
                long value = op.apply(leftVal, rightVal);
                return MonetConstant.createIntConstant(value);
            }
        }

        MonetBinaryOperator(String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        @Override
        public String getTextRepresentation() {
            return textRepresentation;
        }

        public abstract MonetConstant apply(MonetConstant left, MonetConstant right);

        public static MonetBinaryOperator getRandom() {
            return Randomly.fromOptions(values());
        }

    }

    public MonetBinaryArithmeticOperation(MonetExpression left, MonetExpression right,
            MonetBinaryOperator op) {
        super(left, right, op);
    }

    @Override
    public MonetConstant getExpectedValue() {
        MonetConstant leftExpected = getLeft().getExpectedValue();
        MonetConstant rightExpected = getRight().getExpectedValue();
        return getOp().apply(leftExpected, rightExpected);
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.INT;
    }

}
