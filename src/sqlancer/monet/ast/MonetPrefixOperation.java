package sqlancer.monet.ast;

import sqlancer.IgnoreMeException;
import sqlancer.common.ast.BinaryOperatorNode.Operator;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetPrefixOperation implements MonetExpression {

    public enum PrefixOperator implements Operator {
        NOT("NOT", MonetDataType.BOOLEAN) {

            @Override
            protected MonetConstant getExpectedValue(MonetConstant expectedValue) {
                if (expectedValue.isNull()) {
                    return MonetConstant.createNullConstant();
                } else {
                    return MonetConstant.createBooleanConstant(!expectedValue.cast(MonetDataType.BOOLEAN).asBoolean());
                }
            }
        },
        UNARY_PLUS("+", MonetDataType.TINYINT, MonetDataType.SMALLINT, MonetDataType.INT, MonetDataType.BIGINT,
                MonetDataType.HUGEINT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL,
                MonetDataType.SECOND_INTERVAL, MonetDataType.DAY_INTERVAL, MonetDataType.MONTH_INTERVAL) {

            @Override
            protected MonetConstant getExpectedValue(MonetConstant expectedValue) {
                // TODO: actual converts to double precision
                return expectedValue;
            }

        },
        UNARY_MINUS("-", MonetDataType.TINYINT, MonetDataType.SMALLINT, MonetDataType.INT, MonetDataType.BIGINT,
                MonetDataType.HUGEINT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL,
                MonetDataType.SECOND_INTERVAL, MonetDataType.DAY_INTERVAL, MonetDataType.MONTH_INTERVAL) {

            @Override
            protected MonetConstant getExpectedValue(MonetConstant expectedValue) {
                if (expectedValue.isNull()) {
                    // TODO
                    throw new IgnoreMeException();
                }
                if (expectedValue.isInt() && expectedValue.asInt() == Long.MIN_VALUE) {
                    throw new IgnoreMeException();
                }
                try {
                    return MonetConstant.createIntConstant(-expectedValue.asInt(), MonetDataType.INT);
                } catch (UnsupportedOperationException e) {
                    return null;
                }
            }

        };

        private String textRepresentation;
        private MonetDataType[] dataTypes;

        PrefixOperator(String textRepresentation, MonetDataType... dataTypes) {
            this.textRepresentation = textRepresentation;
            this.dataTypes = dataTypes.clone();
        }

        protected abstract MonetConstant getExpectedValue(MonetConstant expectedValue);

        @Override
        public String getTextRepresentation() {
            return toString();
        }

    }

    private final MonetExpression expr;
    private final PrefixOperator op;
    private final MonetDataType type;

    public MonetPrefixOperation(MonetExpression expr, PrefixOperator op, MonetDataType type) {
        this.expr = expr;
        this.op = op;
        this.type = type;
    }

    @Override
    public MonetDataType getExpressionType() {
        return type;
    }

    @Override
    public MonetConstant getExpectedValue() {
        MonetConstant expectedValue = expr.getExpectedValue();
        if (expectedValue == null) {
            return null;
        }
        return op.getExpectedValue(expectedValue);
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
