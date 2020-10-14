package sqlancer.monet.ast;

import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetFunction implements MonetExpression {

    private final String func;
    private final MonetExpression[] args;
    private final MonetDataType returnType;
    private MonetFunctionWithResult functionWithKnownResult;

    public MonetFunction(MonetFunctionWithResult func, MonetDataType returnType, MonetExpression... args) {
        functionWithKnownResult = func;
        this.func = func.getName();
        this.returnType = returnType;
        this.args = args.clone();
    }

    public MonetFunction(MonetFunctionWithUnknownResult f, MonetDataType returnType,
            MonetExpression... args) {
        this.func = f.getName();
        this.returnType = returnType;
        this.args = args.clone();
    }

    public String getFunctionName() {
        return func;
    }

    public MonetExpression[] getArguments() {
        return args.clone();
    }

    public enum MonetFunctionWithResult {
        ABS(1, "abs") {

            @Override
            public MonetConstant apply(MonetConstant[] evaluatedArgs, MonetExpression... args) {
                if (evaluatedArgs[0].isNull()) {
                    return MonetConstant.createNullConstant();
                } else {
                    return MonetConstant.createIntConstant(Math.abs(evaluatedArgs[0].cast(MonetDataType.INT).asInt()), MonetDataType.INT);
                }
            }

            @Override
            public boolean supportsReturnType(MonetDataType type) {
                return type == MonetDataType.TINYINT || type == MonetDataType.SMALLINT || type == MonetDataType.INT ||
                type == MonetDataType.BIGINT || type == MonetDataType.HUGEINT || type == MonetDataType.REAL || type == MonetDataType.DOUBLE || 
                type == MonetDataType.DECIMAL || type == MonetDataType.SECOND_INTERVAL || type == MonetDataType.DAY_INTERVAL || type == MonetDataType.MONTH_INTERVAL;
            }

            @Override
            public MonetDataType[] getInputTypesForReturnType(MonetDataType returnType, int nrArguments) {
                return new MonetDataType[] { returnType };
            }

        },
        SIGN(1, "sign") {

            @Override
            public MonetConstant apply(MonetConstant[] evaluatedArgs, MonetExpression... args) {
                if (evaluatedArgs[0].isNull()) {
                    return MonetConstant.createNullConstant();
                } else {
                    return MonetConstant.createIntConstant(Math.abs(evaluatedArgs[0].cast(MonetDataType.INT).asInt()), MonetDataType.INT);
                }
            }

            @Override
            public boolean supportsReturnType(MonetDataType type) {
                return type == MonetDataType.TINYINT || type == MonetDataType.SMALLINT || type == MonetDataType.INT ||
                type == MonetDataType.BIGINT || type == MonetDataType.HUGEINT || type == MonetDataType.REAL || type == MonetDataType.DOUBLE || 
                type == MonetDataType.DECIMAL || type == MonetDataType.SECOND_INTERVAL || type == MonetDataType.DAY_INTERVAL || type == MonetDataType.MONTH_INTERVAL;
            }

            @Override
            public MonetDataType[] getInputTypesForReturnType(MonetDataType returnType, int nrArguments) {
                return new MonetDataType[] { returnType };
            }

        },
        LOWER(1, "lower") {

            @Override
            public MonetConstant apply(MonetConstant[] evaluatedArgs, MonetExpression... args) {
                if (evaluatedArgs[0].isNull()) {
                    return MonetConstant.createNullConstant();
                } else {
                    String text = evaluatedArgs[0].asString();
                    return MonetConstant.createTextConstant(text.toLowerCase());
                }
            }

            @Override
            public boolean supportsReturnType(MonetDataType type) {
                return type == MonetDataType.STRING;
            }

            @Override
            public MonetDataType[] getInputTypesForReturnType(MonetDataType returnType, int nrArguments) {
                return new MonetDataType[] { MonetDataType.STRING };
            }

        },
        LENGTH(1, "length") {
            @Override
            public MonetConstant apply(MonetConstant[] evaluatedArgs, MonetExpression... args) {
                if (evaluatedArgs[0].isNull()) {
                    return MonetConstant.createNullConstant();
                }
                String text = evaluatedArgs[0].asString();
                return MonetConstant.createIntConstant(text.length(), MonetDataType.INT);
            }

            @Override
            public boolean supportsReturnType(MonetDataType type) {
                return type == MonetDataType.TINYINT || type == MonetDataType.SMALLINT || type == MonetDataType.INT ||
                type == MonetDataType.BIGINT || type == MonetDataType.HUGEINT;
            }

            @Override
            public MonetDataType[] getInputTypesForReturnType(MonetDataType returnType, int nrArguments) {
                return new MonetDataType[] { MonetDataType.STRING };
            }
        },
        UPPER(1, "upper") {

            @Override
            public MonetConstant apply(MonetConstant[] evaluatedArgs, MonetExpression... args) {
                if (evaluatedArgs[0].isNull()) {
                    return MonetConstant.createNullConstant();
                } else {
                    String text = evaluatedArgs[0].asString();
                    return MonetConstant.createTextConstant(text.toUpperCase());
                }
            }

            @Override
            public boolean supportsReturnType(MonetDataType type) {
                return type == MonetDataType.STRING;
            }

            @Override
            public MonetDataType[] getInputTypesForReturnType(MonetDataType returnType, int nrArguments) {
                return new MonetDataType[] { MonetDataType.STRING };
            }

        },
        GREATEST(2, "greatest") {

            @Override
            public MonetConstant apply(MonetConstant[] evaluatedArgs, MonetExpression... args) {
                throw new AssertionError(this);
            }

            @Override
            public boolean supportsReturnType(MonetDataType type) {
                return true;
            }

            @Override
            public MonetDataType[] getInputTypesForReturnType(MonetDataType returnType, int nrArguments) {
                return new MonetDataType[] { returnType, returnType };
            }

        };

        private String functionName;
        final int nrArgs;
        private final boolean variadic;

        public MonetDataType[] getRandomTypes(int nr) {
            MonetDataType[] types = new MonetDataType[nr];
            for (int i = 0; i < types.length; i++) {
                types[i] = MonetDataType.getRandomType();
            }
            return types;
        }

        MonetFunctionWithResult(int nrArgs, String functionName) {
            this.nrArgs = nrArgs;
            this.functionName = functionName;
            this.variadic = false;
        }

        /**
         * Gets the number of arguments if the function is non-variadic. If the function is variadic, the minimum number
         * of arguments is returned.
         *
         * @return the number of arguments
         */
        public int getNrArgs() {
            return nrArgs;
        }

        public abstract MonetConstant apply(MonetConstant[] evaluatedArgs, MonetExpression... args);

        @Override
        public String toString() {
            return functionName;
        }

        public boolean isVariadic() {
            return variadic;
        }

        public String getName() {
            return functionName;
        }

        public abstract boolean supportsReturnType(MonetDataType type);

        public abstract MonetDataType[] getInputTypesForReturnType(MonetDataType returnType, int nrArguments);

        public boolean checkArguments(MonetExpression... constants) {
            return true;
        }

    }

    @Override
    public MonetConstant getExpectedValue() {
        if (functionWithKnownResult == null) {
            return null;
        }
        MonetConstant[] constants = new MonetConstant[args.length];
        for (int i = 0; i < constants.length; i++) {
            constants[i] = args[i].getExpectedValue();
            if (constants[i] == null) {
                return null;
            }
        }
        return functionWithKnownResult.apply(constants, args);
    }

    @Override
    public MonetDataType getExpressionType() {
        return returnType;
    }

}
