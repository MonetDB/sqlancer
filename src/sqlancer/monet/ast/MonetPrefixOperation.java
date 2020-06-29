package sqlancer.monet.ast;

import sqlancer.IgnoreMeException;
import sqlancer.ast.BinaryOperatorNode.Operator;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetPrefixOperation implements MonetExpression {

    public enum PrefixOperator implements Operator {
        NOT("NOT", MonetDataType.BOOLEAN) {

            @Override
            public MonetDataType getExpressionType() {
                return MonetDataType.BOOLEAN;
            }

            @Override
            protected MonetConstant getExpectedValue(MonetConstant expectedValue) {
                if (expectedValue.isNull()) {
                    return MonetConstant.createNullConstant();
                } else {
                    return MonetConstant
                            .createBooleanConstant(!expectedValue.cast(MonetDataType.BOOLEAN).asBoolean());
                }
            }
        },
        UNARY_PLUS("+", MonetDataType.INT) {

            @Override
            public MonetDataType getExpressionType() {
                return MonetDataType.INT;
            }

            @Override
            protected MonetConstant getExpectedValue(MonetConstant expectedValue) {
                // TODO: actual converts to double precision
                return expectedValue;
            }

        },
        UNARY_MINUS("-", MonetDataType.INT) {

            @Override
            public MonetDataType getExpressionType() {
                return MonetDataType.INT;
            }

            @Override
            protected MonetConstant getExpectedValue(MonetConstant expectedValue) {
                if (expectedValue.isNull()) {
                    // TODO
                    throw new IgnoreMeException();
                }
                return MonetConstant.createIntConstant(-expectedValue.asInt());
            }

        };

        private String textRepresentation;
        private MonetDataType[] dataTypes;

        PrefixOperator(String textRepresentation, MonetDataType... dataTypes) {
            this.textRepresentation = textRepresentation;
            this.dataTypes = dataTypes.clone();
        }

        public abstract MonetDataType getExpressionType();

        protected abstract MonetConstant getExpectedValue(MonetConstant expectedValue);

        @Override
        public String getTextRepresentation() {
            return toString();
        }

    }

    private final MonetExpression expr;
    private final PrefixOperator op;

    public MonetPrefixOperation(MonetExpression expr, PrefixOperator op) {
        this.expr = expr;
        this.op = op;
    }

    @Override
    public MonetDataType getExpressionType() {
        return op.getExpressionType();
    }

    @Override
    public MonetConstant getExpectedValue() {
        return op.getExpectedValue(expr.getExpectedValue());
    }

    public MonetDataType[] getInputDataTypes() {
        return op.dataTypes;
    }

    public String getTextRepresentation() {
        return op.textRepresentation;
    }

    public MonetExpression getExpression() {
        return expr;
    }

}
