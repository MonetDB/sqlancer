package sqlancer.monet.ast;

import java.util.List;

import sqlancer.Randomly;
import sqlancer.monet.MonetSchema.MonetDataType;

public final class MonetAnyTypeOperation implements MonetExpression {

    private final MonetAnyTypeOperationType function;

    private final List<MonetExpression> arguments;

    public enum MonetAnyTypeOperationType {
        COALESCE("COALESCE", 0), IFTHENELSE("ifthenelse", 3), NULLIF("NULLIF", 2),
        GREATEST("greatest", 2), LEAST("least", 2), SQL_MIN("sql_min", 2), SQL_MAX("sql_max", 2);

        private String functionName;

        private int nArgs; /* 0 means any */

        MonetAnyTypeOperationType(String functionName, int nArgs) {
            this.functionName = functionName;
            this.nArgs = nArgs;
        }

        public String getName() {
            return functionName;
        }

        public int getNargs() {
            return nArgs;
        }

        public static MonetAnyTypeOperationType getRandom() {
            return Randomly.fromOptions(values());
        }
    }

    public MonetAnyTypeOperation(MonetAnyTypeOperationType function, List<MonetExpression> arguments) {
        this.function = function;
        this.arguments = arguments;
        if ((function.getNargs() == 0 && arguments.size() < 2) || (function.getNargs() > 0 && arguments.size() != function.getNargs()))
            throw new AssertionError(this);
    }

    public MonetAnyTypeOperationType getFunction() {
        return function;
    }

    public List<MonetExpression> getArguments() {
        return arguments;
    }

    @Override
    public MonetConstant getExpectedValue() {
        throw new AssertionError(this);
    }

    @Override
    public MonetDataType getExpressionType() {
        return null;
    }

}
