package sqlancer.monet.ast;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.ast.FunctionNode;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetAggregate.MonetAggregateFunction;

/**
 * @see https://www.sqlite.org/lang_aggfunc.html
 */
public class MonetAggregate extends FunctionNode<MonetAggregateFunction, MonetExpression>
        implements MonetExpression {

    private final boolean isDistinct;

    public enum MonetAggregateFunction {
        AVG(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL), 
        COUNT(1), MAX(1), MIN(1),
        LISTAGG(1, MonetDataType.STRING),
        GROUP_CONCAT(1, MonetDataType.STRING),
        SUM(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        PROD(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL),
        MEDIAN(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        MEDIAN_AVG(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),

        STDDEV_SAMP(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        STDDEV_POP(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        VAR_POP(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        VAR_SAMP(1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),

        COVAR_POP(2, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        COVAR_SAMP(2, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        CORR(2, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL);

        private final int nrArgs;

        private MonetDataType[] supportedReturnTypes;

        MonetAggregateFunction(int nrArgs, MonetDataType... supportedReturnTypes) {
            this.supportedReturnTypes = supportedReturnTypes.clone();
            this.nrArgs = nrArgs;
        }

        public static MonetAggregateFunction getRandom() {
            return Randomly.fromOptions(values());
        }

        public static MonetAggregateFunction getRandom(MonetDataType type) {
            return Randomly.fromOptions(values());
        }

        public List<MonetDataType> getTypes(MonetDataType returnType) {
            return Arrays.asList(returnType);
        }

        public boolean supportsReturnType(MonetDataType returnType) {
            return Arrays.asList(supportedReturnTypes).stream().anyMatch(t -> t == returnType)
                    || supportedReturnTypes.length == 0;
        }

        public static List<MonetAggregateFunction> getAggregates(MonetDataType type) {
            return Arrays.asList(values()).stream().filter(p -> p.supportsReturnType(type))
                    .collect(Collectors.toList());
        }

        public MonetDataType getRandomReturnType() {
            if (supportedReturnTypes.length == 0) {
                return Randomly.fromOptions(MonetDataType.getRandomType());
            } else {
                return Randomly.fromOptions(supportedReturnTypes);
            }
        }

        public int getNrArgs() {
            return nrArgs;
        }
    }

    public MonetAggregate(List<MonetExpression> args, MonetAggregateFunction func, boolean isDistinct) {
        super(func, args);
        this.isDistinct = isDistinct;
    }

    public boolean isDistinct() {
        return this.isDistinct;
    }
}
