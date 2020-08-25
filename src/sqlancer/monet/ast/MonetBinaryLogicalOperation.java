package sqlancer.monet.ast;

import sqlancer.Randomly;
import sqlancer.common.ast.BinaryOperatorNode;
import sqlancer.common.ast.BinaryOperatorNode.Operator;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetBinaryLogicalOperation.BinaryLogicalOperator;

public class MonetBinaryLogicalOperation extends BinaryOperatorNode<MonetExpression, BinaryLogicalOperator>
        implements MonetExpression {

    public enum BinaryLogicalOperator implements Operator {
        AND {
            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                MonetConstant leftBool = left.cast(MonetDataType.BOOLEAN);
                MonetConstant rightBool = right.cast(MonetDataType.BOOLEAN);
                if (leftBool.isNull()) {
                    if (rightBool.isNull()) {
                        return MonetConstant.createNullConstant();
                    } else {
                        if (rightBool.asBoolean()) {
                            return MonetConstant.createNullConstant();
                        } else {
                            return MonetConstant.createFalse();
                        }
                    }
                } else if (!leftBool.asBoolean()) {
                    return MonetConstant.createFalse();
                }
                assert leftBool.asBoolean();
                if (rightBool.isNull()) {
                    return MonetConstant.createNullConstant();
                } else {
                    return MonetConstant.createBooleanConstant(rightBool.isBoolean() && rightBool.asBoolean());
                }
            }
        },
        OR {
            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                MonetConstant leftBool = left.cast(MonetDataType.BOOLEAN);
                MonetConstant rightBool = right.cast(MonetDataType.BOOLEAN);
                if (leftBool.isBoolean() && leftBool.asBoolean()) {
                    return MonetConstant.createTrue();
                }
                if (rightBool.isBoolean() && rightBool.asBoolean()) {
                    return MonetConstant.createTrue();
                }
                if (leftBool.isNull() || rightBool.isNull()) {
                    return MonetConstant.createNullConstant();
                }
                return MonetConstant.createFalse();
            }
        };

        public abstract MonetConstant apply(MonetConstant left, MonetConstant right);

        public static BinaryLogicalOperator getRandom() {
            return Randomly.fromOptions(values());
        }

        @Override
        public String getTextRepresentation() {
            return toString();
        }
    }

    public MonetBinaryLogicalOperation(MonetExpression left, MonetExpression right, BinaryLogicalOperator op) {
        super(left, right, op);
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }

    @Override
    public MonetConstant getExpectedValue() {
        MonetConstant leftExpectedValue = getLeft().getExpectedValue();
        MonetConstant rightExpectedValue = getRight().getExpectedValue();
        if (leftExpectedValue == null || rightExpectedValue == null) {
            return null;
        }
        return getOp().apply(leftExpectedValue, rightExpectedValue);
    }

}
