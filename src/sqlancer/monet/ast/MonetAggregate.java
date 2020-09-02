package sqlancer.monet.ast;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.common.ast.FunctionNode;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetAggregate.MonetAggregateFunction;

/**
 * @see https://www.sqlite.org/lang_aggfunc.html
 */
public class MonetAggregate extends FunctionNode<MonetAggregateFunction, MonetExpression>
        implements MonetExpression {

    private final boolean isDistinct;

    public enum MonetAggregateFunction {
        AVG("avg", 1, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL), 
        COUNT("count", 1, MonetDataType.INT), COUNT_ALL("count", 0), MAX("max", 1), MIN("min", 1),
        LISTAGG("listagg", 1, MonetDataType.STRING),
        GROUP_CONCAT("group_concat", 1, MonetDataType.STRING),
        SUM("sum", 1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        PROD("prod", 1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL),
        MEDIAN("median", 1, MonetDataType.INT, MonetDataType.DOUBLE, MonetDataType.REAL, MonetDataType.DECIMAL, MonetDataType.SECOND_INTERVAL, MonetDataType.MONTH_INTERVAL),
        MEDIAN_AVG("median_avg", 1, MonetDataType.DOUBLE, MonetDataType.REAL),

        STDDEV_POP("stddev_pop", 1, MonetDataType.DOUBLE, MonetDataType.REAL),
        STDDEV_SAMP("stddev_samp", 1, MonetDataType.DOUBLE, MonetDataType.REAL),
        VAR_POP("var_pop", 1, MonetDataType.DOUBLE, MonetDataType.REAL),
        VAR_SAMP("var_samp", 1, MonetDataType.DOUBLE, MonetDataType.REAL),

        COVAR_POP("covar_pop", 2, MonetDataType.DOUBLE, MonetDataType.REAL),
        COVAR_SAMP("covar_samp", 2, MonetDataType.DOUBLE, MonetDataType.REAL),
        CORR("corr", 2, MonetDataType.DOUBLE, MonetDataType.REAL);

        private final int nrArgs;

        private final String name;

        private MonetDataType[] supportedReturnTypes;

        MonetAggregateFunction(String name, int nrArgs, MonetDataType... supportedReturnTypes) {
            this.supportedReturnTypes = supportedReturnTypes.clone();
            this.nrArgs = nrArgs;
            this.name = name;
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

        public String getName() {
            return name;
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
