package sqlancer.monet.ast;

import sqlancer.Randomly;
import sqlancer.common.ast.BinaryOperatorNode;
import sqlancer.common.ast.BinaryOperatorNode.Operator;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetBinaryComparisonOperation.MonetBinaryComparisonOperator;

public class MonetBinaryComparisonOperation
        extends BinaryOperatorNode<MonetExpression, MonetBinaryComparisonOperator> implements MonetExpression {

    public enum MonetBinaryComparisonOperator implements Operator {
        EQUALS("=") {
            @Override
            public MonetConstant getExpectedValue(MonetConstant leftVal, MonetConstant rightVal) {
                return leftVal.isEquals(rightVal);
            }
        },
        NOT_EQUALS("<>") {
            @Override
            public MonetConstant getExpectedValue(MonetConstant leftVal, MonetConstant rightVal) {
                MonetConstant isEquals = leftVal.isEquals(rightVal);
                if (isEquals.isBoolean()) {
                    return MonetConstant.createBooleanConstant(!isEquals.asBoolean());
                }
                return isEquals;
            }
        },
        LESS("<") {

            @Override
            public MonetConstant getExpectedValue(MonetConstant leftVal, MonetConstant rightVal) {
                return leftVal.isLessThan(rightVal);
            }
        },
        LESS_EQUALS("<=") {

            @Override
            public MonetConstant getExpectedValue(MonetConstant leftVal, MonetConstant rightVal) {
                MonetConstant lessThan = leftVal.isLessThan(rightVal);
                if (lessThan.isBoolean() && !lessThan.asBoolean()) {
                    return leftVal.isEquals(rightVal);
                } else {
                    return lessThan;
                }
            }
        },
        GREATER(">") {
            @Override
            public MonetConstant getExpectedValue(MonetConstant leftVal, MonetConstant rightVal) {
                MonetConstant equals = leftVal.isEquals(rightVal);
                if (equals.isBoolean() && equals.asBoolean()) {
                    return MonetConstant.createFalse();
                } else {
                    MonetConstant applyLess = leftVal.isLessThan(rightVal);
                    if (applyLess.isNull()) {
                        return MonetConstant.createNullConstant();
                    }
                    return MonetPrefixOperation.PrefixOperator.NOT.getExpectedValue(applyLess);
                }
            }
        },
        GREATER_EQUALS(">=") {

            @Override
            public MonetConstant getExpectedValue(MonetConstant leftVal, MonetConstant rightVal) {
                MonetConstant equals = leftVal.isEquals(rightVal);
                if (equals.isBoolean() && equals.asBoolean()) {
                    return MonetConstant.createTrue();
                } else {
                    MonetConstant applyLess = leftVal.isLessThan(rightVal);
                    if (applyLess.isNull()) {
                        return MonetConstant.createNullConstant();
                    }
                    return MonetPrefixOperation.PrefixOperator.NOT.getExpectedValue(applyLess);
                }
            }

        };

        private final String textRepresentation;

        @Override
        public String getTextRepresentation() {
            return textRepresentation;
        }

        MonetBinaryComparisonOperator(String textRepresentation) {
            this.textRepresentation = textRepresentation;
        }

        public abstract MonetConstant getExpectedValue(MonetConstant leftVal, MonetConstant rightVal);

        public static MonetBinaryComparisonOperator getRandom() {
            return Randomly.fromOptions(MonetBinaryComparisonOperator.values());
        }

    }

    public MonetBinaryComparisonOperation(MonetExpression left, MonetExpression right,
            MonetBinaryComparisonOperator op) {
        super(left, right, op);
    }

    @Override
    public MonetConstant getExpectedValue() {
        return getOp().getExpectedValue(getLeft().getExpectedValue(), getRight().getExpectedValue());
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }

}
