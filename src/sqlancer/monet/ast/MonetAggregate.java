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
        AVG(MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL), 
        COUNT, MAX, MIN,
        LISTAGG(MonetDataType.STRING),
        GROUP_CONCAT(MonetDataType.STRING),
        SUM(MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        PROD(MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL),
        STDDEV_SAMP(MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL),
        STDDEV_POP(MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL), 
        VAR_POP(MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL),
        VAR_SAMP(MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL);

        private MonetDataType[] supportedReturnTypes;

        MonetAggregateFunction(MonetDataType... supportedReturnTypes) {
            this.supportedReturnTypes = supportedReturnTypes.clone();
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

    }

    public MonetAggregate(List<MonetExpression> args, MonetAggregateFunction func, boolean isDistinct) {
        super(func, args);
        this.isDistinct = isDistinct;
    }

    public boolean isDistinct() {
        return this.isDistinct;
    }
}
