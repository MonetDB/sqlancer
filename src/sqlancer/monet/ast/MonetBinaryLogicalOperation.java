package sqlancer.monet.ast;

import sqlancer.Randomly;
import sqlancer.ast.BinaryOperatorNode;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetBinaryLogicalOperation.BinaryLogicalOperator;

public class MonetBinaryLogicalOperation extends BinaryOperatorNode<MonetExpression, BinaryLogicalOperator>
        implements MonetExpression {

    public enum BinaryLogicalOperator implements Operator {
        AND {
            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                left = left.cast(MonetDataType.BOOLEAN);
                right = right.cast(MonetDataType.BOOLEAN);
                if (left.isNull()) {
                    if (right.isNull()) {
                        return MonetConstant.createNullConstant();
                    } else {
                        if (right.asBoolean()) {
                            return MonetConstant.createNullConstant();
                        } else {
                            return MonetConstant.createFalse();
                        }
                    }
                } else if (!left.asBoolean()) {
                    return MonetConstant.createFalse();
                }
                assert left.asBoolean();
                if (right.isNull()) {
                    return MonetConstant.createNullConstant();
                } else {
                    return MonetConstant.createBooleanConstant(right.isBoolean() && right.asBoolean());
                }
            }
        },
        OR {
            @Override
            public MonetConstant apply(MonetConstant left, MonetConstant right) {
                left = left.cast(MonetDataType.BOOLEAN);
                right = right.cast(MonetDataType.BOOLEAN);
                if (left.isBoolean() && left.asBoolean()) {
                    return MonetConstant.createTrue();
                }
                if (right.isBoolean() && right.asBoolean()) {
                    return MonetConstant.createTrue();
                }
                if (left.isNull() || right.isNull()) {
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
        return getOp().apply(getLeft().getExpectedValue(), getRight().getExpectedValue());
    }

}
