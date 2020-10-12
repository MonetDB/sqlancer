package sqlancer.monet.ast;

import sqlancer.Randomly;
import sqlancer.common.ast.BinaryOperatorNode.Operator;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetPostfixOperation implements MonetExpression {

    private final MonetExpression expr;
    private final PostfixOperator op;
    private final String operatorTextRepresentation;

    public enum PostfixOperator implements Operator {
        IS_NULL("IS NULL") {
            @Override
            public MonetConstant apply(MonetConstant expectedValue) {
                return MonetConstant.createBooleanConstant(expectedValue.isNull());
            }

            @Override
            public MonetDataType[] getInputDataTypes() {
                MonetDataType[] typesArray = new MonetDataType[MonetDataType.getAllTypes().size()];
                return MonetDataType.getAllTypes().toArray(typesArray);
            }

        },
        IS_NOT_NULL("IS NOT NULL") {

            @Override
            public MonetConstant apply(MonetConstant expectedValue) {
                return MonetConstant.createBooleanConstant(!expectedValue.isNull());
            }

            @Override
            public MonetDataType[] getInputDataTypes() {
                MonetDataType[] typesArray = new MonetDataType[MonetDataType.getAllTypes().size()];
                return MonetDataType.getAllTypes().toArray(typesArray);
            }

        },
        IS_TRUE("= TRUE") {

            @Override
            public MonetConstant apply(MonetConstant expectedValue) {
                if (expectedValue.isNull()) {
                    return MonetConstant.createFalse();
                } else {
                    return MonetConstant
                            .createBooleanConstant(expectedValue.cast(MonetDataType.BOOLEAN).asBoolean());
                }
            }

            @Override
            public MonetDataType[] getInputDataTypes() {
                return new MonetDataType[] { MonetDataType.BOOLEAN };
            }

        },
        IS_FALSE("= FALSE") {

            @Override
            public MonetConstant apply(MonetConstant expectedValue) {
                if (expectedValue.isNull()) {
                    return MonetConstant.createFalse();
                } else {
                    return MonetConstant
                            .createBooleanConstant(!expectedValue.cast(MonetDataType.BOOLEAN).asBoolean());
                }
            }

            @Override
            public MonetDataType[] getInputDataTypes() {
                return new MonetDataType[] { MonetDataType.BOOLEAN };
            }

        };

        private String[] textRepresentations;

        PostfixOperator(String... textRepresentations) {
            this.textRepresentations = textRepresentations.clone();
        }

        public abstract MonetConstant apply(MonetConstant expectedValue);

        public abstract MonetDataType[] getInputDataTypes();

        public static PostfixOperator getRandom() {
            return Randomly.fromOptions(values());
        }

        @Override
        public String getTextRepresentation() {
            return toString();
        }
    }

    public MonetPostfixOperation(MonetExpression expr, PostfixOperator op) {
        this.expr = expr;
        this.operatorTextRepresentation = Randomly.fromOptions(op.textRepresentations);
        this.op = op;
    }

    @Override
    public MonetDataType getExpressionType() {
        return MonetDataType.BOOLEAN;
    }

    @Override
    public MonetConstant getExpectedValue() {
        MonetConstant expectedValue = expr.getExpectedValue();
        if (expectedValue == null) {
            return null;
        }
        return op.apply(expectedValue);
    }

    public String getOperatorTextRepresentation() {
        return operatorTextRepresentation;
    }

    public static MonetExpression create(MonetExpression expr, PostfixOperator op) {
        return new MonetPostfixOperation(expr, op);
    }

    public MonetExpression getExpression() {
        return expr;
    }

}
