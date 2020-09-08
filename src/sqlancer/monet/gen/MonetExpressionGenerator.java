package sqlancer.monet.gen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import sqlancer.IgnoreMeException;
import sqlancer.Randomly;
import sqlancer.common.gen.ExpressionGenerator;
import sqlancer.monet.MonetCompoundDataType;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetProvider;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetRowValue;
import sqlancer.monet.ast.MonetAggregate;
import sqlancer.monet.ast.MonetAggregate.MonetAggregateFunction;
import sqlancer.monet.ast.MonetAnyAllOperation;
import sqlancer.monet.ast.MonetBetweenOperation;
import sqlancer.monet.ast.MonetBinaryArithmeticOperation;
import sqlancer.monet.ast.MonetBinaryArithmeticOperation.MonetBinaryOperator;
import sqlancer.monet.ast.MonetBinaryBitOperation;
import sqlancer.monet.ast.MonetBinaryBitOperation.MonetBinaryBitOperator;
import sqlancer.monet.ast.MonetBinaryComparisonOperation;
import sqlancer.monet.ast.MonetBinaryLogicalOperation;
import sqlancer.monet.ast.MonetBinaryLogicalOperation.BinaryLogicalOperator;
import sqlancer.monet.ast.MonetCaseOperation;
import sqlancer.monet.ast.MonetCoalesceOperation;
import sqlancer.monet.ast.MonetCastOperation;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetConcatOperation;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExistsOperation;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetFunction;
import sqlancer.monet.ast.MonetFunction.MonetFunctionWithResult;
import sqlancer.monet.ast.MonetFunctionWithUnknownResult;
import sqlancer.monet.ast.MonetInOperation;
import sqlancer.monet.ast.MonetLikeOperation;
import sqlancer.monet.ast.MonetOrderByTerm;
import sqlancer.monet.ast.MonetOrderByTerm.MonetNullsFirstOrLast;
import sqlancer.monet.ast.MonetOrderByTerm.MonetOrder;
import sqlancer.monet.ast.MonetPostfixOperation;
import sqlancer.monet.ast.MonetPostfixOperation.PostfixOperator;
import sqlancer.monet.ast.MonetPrefixOperation;
import sqlancer.monet.ast.MonetPrefixOperation.PrefixOperator;
import sqlancer.monet.ast.MonetSelect;

public class MonetExpressionGenerator implements ExpressionGenerator<MonetExpression> {

    private final int maxDepth;

    private final Randomly r;

    private List<MonetColumn> columns;

    private MonetRowValue rw;

    /*private boolean expectedResult;*/

    private MonetGlobalState globalState;

    private boolean allowAggregateFunctions;

    private boolean allowParameters;

    private final Map<String, Character> functionsAndTypes;

    private final List<Character> allowedFunctionTypes;

    public MonetExpressionGenerator(MonetGlobalState globalState) {
        this.r = globalState.getRandomly();
        this.maxDepth = globalState.getOptions().getMaxExpressionDepth();
        this.globalState = globalState;
        this.functionsAndTypes = globalState.getFunctionsAndTypes();
        this.allowedFunctionTypes = globalState.getAllowedFunctionTypes();
    }

    public MonetExpressionGenerator setColumns(List<MonetColumn> columns) {
        this.columns = columns;
        return this;
    }

    public MonetExpressionGenerator setRowValue(MonetRowValue rw) {
        this.rw = rw;
        return this;
    }

    public MonetExpressionGenerator setAllowParameters(Boolean allowParameters) {
        this.allowParameters = allowParameters;
        return this;
    }

    public MonetExpression generateExpression(int depth) {
        return generateExpression(depth, MonetDataType.getRandomType());
    }

    public List<MonetExpression> generateExpressions(int depth, int nr) {
        List<MonetExpression> expressions = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            expressions.add(generateExpression(depth));
        }
        return expressions;
    }

    public List<MonetExpression> generateExpressions(int depth, int nr, MonetDataType originalType) {
        List<MonetExpression> expressions = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            expressions.add(generateExpression(depth, originalType));
        }
        return expressions;
    }

    public List<MonetExpression> generateOrderBy() {
        List<MonetExpression> orderBys = new ArrayList<>();
        for (int i = 0; i < Randomly.smallNumber(); i++) {
            orderBys.add(new MonetOrderByTerm(MonetColumnValue.create(Randomly.fromList(columns), null),
                    MonetOrder.getRandomOrder(), MonetNullsFirstOrLast.getRandomNullsOrder()));
        }
        return orderBys;
    }

    private enum BooleanExpression {
        POSTFIX_OPERATOR, NOT, BINARY_LOGICAL_OPERATOR, BINARY_COMPARISON, FUNCTION, CAST, LIKE, BETWEEN, IN_OPERATION, EXISTS, ANYALL, CASE, COALESCE, SUBQUERY
    }

    private MonetExpression generateFunctionWithUnknownResult(int depth, MonetDataType type) {
        List<MonetFunctionWithUnknownResult> supportedFunctions = MonetFunctionWithUnknownResult
                .getSupportedFunctions(type);
        // filters functions by allowed type (STABLE 's', IMMUTABLE 'i', VOLATILE 'v')
        supportedFunctions = supportedFunctions.stream()
                .filter(f -> allowedFunctionTypes.contains(functionsAndTypes.get(f.getName())))
                .collect(Collectors.toList());
        if (supportedFunctions.isEmpty()) {
            throw new IgnoreMeException();
        }
        MonetFunctionWithUnknownResult randomFunction = Randomly.fromList(supportedFunctions);
        return new MonetFunction(randomFunction, type, randomFunction.getArguments(type, this, depth + 1));
    }

    private MonetExpression generateFunctionWithKnownResult(int depth, MonetDataType type) {
        List<MonetFunctionWithResult> functions = Stream.of(MonetFunction.MonetFunctionWithResult.values())
                .filter(f -> f.supportsReturnType(type)).collect(Collectors.toList());
        // filters functions by allowed type (STABLE 's', IMMUTABLE 'i', VOLATILE 'v')
        functions = functions.stream().filter(f -> allowedFunctionTypes.contains(functionsAndTypes.get(f.getName())))
                .collect(Collectors.toList());
        if (functions.isEmpty()) {
            throw new IgnoreMeException();
        }
        MonetFunctionWithResult randomFunction = Randomly.fromList(functions);
        int nrArgs = randomFunction.getNrArgs();
        if (randomFunction.isVariadic()) {
            nrArgs += Randomly.smallNumber();
        }
        MonetDataType[] argTypes = randomFunction.getInputTypesForReturnType(type, nrArgs);
        MonetExpression[] args = new MonetExpression[nrArgs];
        do {
            for (int i = 0; i < args.length; i++) {
                args[i] = generateExpression(depth + 1, argTypes[i]);
            }
        } while (!randomFunction.checkArguments(args));
        return new MonetFunction(randomFunction, type, args);
    }

    private MonetExpression generateBooleanExpression(int depth) {
        List<BooleanExpression> validOptions = new ArrayList<>(Arrays.asList(BooleanExpression.values()));
        /*if (MonetProvider.generateOnlyKnown) {
            validOptions.remove(BooleanExpression.BINARY_RANGE_COMPARISON);
        }*/
        BooleanExpression option = Randomly.fromList(validOptions);
        switch (option) {
        case POSTFIX_OPERATOR:
            PostfixOperator random = PostfixOperator.getRandom();
            return MonetPostfixOperation
                    .create(generateExpression(depth + 1, Randomly.fromOptions(random.getInputDataTypes())), random);
        case IN_OPERATION:
            return inOperation(depth + 1);
        case NOT:
            return new MonetPrefixOperation(generateExpression(depth + 1, MonetDataType.BOOLEAN),
                    PrefixOperator.NOT);
        case BINARY_LOGICAL_OPERATOR:
            MonetExpression first = generateExpression(depth + 1, MonetDataType.BOOLEAN);
            int nr = Randomly.smallNumber() + 1;
            for (int i = 0; i < nr; i++) {
                first = new MonetBinaryLogicalOperation(first,
                        generateExpression(depth + 1, MonetDataType.BOOLEAN), BinaryLogicalOperator.getRandom());
            }
            return first;
        case BINARY_COMPARISON:
            MonetDataType dataType = getMeaningfulType();
            return generateComparison(depth, dataType);
        case CAST:
            return new MonetCastOperation(generateExpression(depth + 1),
                    getCompoundDataType(MonetDataType.BOOLEAN));
        case FUNCTION:
            return generateFunction(depth + 1, MonetDataType.BOOLEAN);
        case LIKE:
            return new MonetLikeOperation(generateExpression(depth + 1, MonetDataType.STRING),
                    generateExpression(depth + 1, MonetDataType.STRING), Randomly.getBoolean(), Randomly.getBoolean());
        case BETWEEN:
            MonetDataType type = getMeaningfulType();
            return new MonetBetweenOperation(generateExpression(depth + 1, type),
                    generateExpression(depth + 1, type), generateExpression(depth + 1, type), Randomly.getBoolean(), Randomly.getBoolean());
        case ANYALL:
            MonetDataType t = getMeaningfulType();
            MonetSelect sel1 = MonetRandomQueryGenerator.createRandomSingleColumnQuery(depth + 1, t, globalState, false, false, this.allowParameters);
            return new MonetAnyAllOperation(generateExpression(depth + 1, t), MonetBinaryComparisonOperation.MonetBinaryComparisonOperator.getRandom(), Randomly.getBoolean(), sel1);
        case EXISTS:
            int nrColumns = Randomly.smallNumber() + 1;
            MonetSelect sel2 = MonetRandomQueryGenerator.createRandomQuery(depth + 1, nrColumns, globalState, false, false, this.allowParameters);
            return new MonetExistsOperation(sel2, Randomly.getBoolean());
        case SUBQUERY:
            MonetSelect sel3 = MonetRandomQueryGenerator.createRandomSingleColumnQuery(depth + 1, MonetDataType.BOOLEAN, globalState, false, false, this.allowParameters);
            return new MonetSelect.MonetSubquery(sel3, null);
        case CASE:
            MonetDataType tp = Randomly.fromOptions(MonetDataType.values());
            int noptions = Randomly.smallNumber() + 1;
            return new MonetCaseOperation(Randomly.getBoolean() ? generateExpression(depth + 1, tp) : null,
                    generateExpressions(depth + 1, noptions, tp), generateExpressions(depth + 1, noptions, MonetDataType.BOOLEAN),
                    Randomly.getBoolean() ? generateExpression(depth + 1, MonetDataType.BOOLEAN) : null);
        case COALESCE:
            int options = Randomly.smallNumber() + 2;
            return new MonetCoalesceOperation(generateExpressions(depth + 1, options, MonetDataType.BOOLEAN));
        default:
            throw new AssertionError();
        }
    }

    private MonetDataType getMeaningfulType() {
        // make it more likely that the expression does not only consist of constant
        // expressions
        if (Randomly.getBooleanWithSmallProbability() || columns == null || columns.isEmpty()) {
            return MonetDataType.getRandomType();
        } else {
            return Randomly.fromList(columns).getType();
        }
    }

    private MonetExpression generateFunction(int depth, MonetDataType type) {
        if (MonetProvider.generateOnlyKnown || Randomly.getBoolean()) {
            return generateFunctionWithKnownResult(depth, type);
        } else {
            return generateFunctionWithUnknownResult(depth, type);
        }
    }

    private MonetExpression generateComparison(int depth, MonetDataType dataType) {
        MonetExpression leftExpr = generateExpression(depth + 1, dataType);
        MonetExpression rightExpr = generateExpression(depth + 1, dataType);
        return getComparison(leftExpr, rightExpr);
    }

    private MonetExpression getComparison(MonetExpression leftExpr, MonetExpression rightExpr) {
        MonetBinaryComparisonOperation op = new MonetBinaryComparisonOperation(leftExpr, rightExpr,
                MonetBinaryComparisonOperation.MonetBinaryComparisonOperator.getRandom());
        return op;
    }

    private MonetExpression inOperation(int depth) {
        MonetDataType type = MonetDataType.getRandomType();
        MonetExpression leftExpr = generateExpression(depth + 1, type);
        List<MonetExpression> rightExpr = new ArrayList<>();
        for (int i = 0; i < Randomly.smallNumber() + 1; i++) {
            rightExpr.add(generateExpression(depth + 1, type));
        }
        return new MonetInOperation(leftExpr, rightExpr, Randomly.getBoolean());
    }

    public static MonetExpression generateExpression(MonetGlobalState globalState, MonetDataType type) {
        return new MonetExpressionGenerator(globalState).generateExpression(0, type);
    }

    public MonetExpression generateExpression(int depth, MonetDataType originalType) {
        MonetDataType dataType = originalType;
        if (dataType == MonetDataType.DOUBLE && Randomly.getBoolean()) {
            dataType = Randomly.fromOptions(MonetDataType.INT, MonetDataType.DOUBLE);
        }
        if (dataType == MonetDataType.REAL && Randomly.getBoolean()) {
            dataType = MonetDataType.INT;
        }
        if (!filterColumns(dataType).isEmpty() && Randomly.getBoolean()) {
            return createColumnOfType(dataType);
        }
        if (allowAggregateFunctions && Randomly.getBoolean()) {
            allowAggregateFunctions = false; // aggregate function calls cannot be nested
            return getAggregate(dataType);
        }
        if (allowParameters && Randomly.getBoolean()) {
            return MonetConstant.createParameterConstant();
        }
        if (Randomly.getBooleanWithRatherLowProbability() || depth > maxDepth) {
            // generic expression
            if (Randomly.getBoolean() || depth > maxDepth) {
                if (Randomly.getBooleanWithRatherLowProbability()) {
                    return generateConstant(r, dataType);
                } else {
                    if (filterColumns(dataType).isEmpty()) {
                        return generateConstant(r, dataType);
                    } else {
                        return createColumnOfType(dataType);
                    }
                }
            } else {
                if (Randomly.getBoolean()) {
                    return new MonetCastOperation(generateExpression(depth + 1), getCompoundDataType(dataType));
                } else {
                    return generateFunctionWithUnknownResult(depth, dataType);
                }
            }
        } else {
            switch (dataType) {
            case BOOLEAN:
                return generateBooleanExpression(depth);
            case INT:
                if (Randomly.getBooleanWithRatherLowProbability()) {
                    return generateBitExpression(depth);
                } else {
                    return generateIntExpression(depth);
                }
            case STRING:
            case DECIMAL:
            case REAL:
            case DOUBLE:
            case TIME:
            case TIMESTAMP:
            case DATE:
            case MONTH_INTERVAL:
            case SECOND_INTERVAL:
            case BLOB:
                return generateAnyTypeExpression(depth, r, dataType);
            default:
                throw new AssertionError(dataType);
            }
        }
    }

    private static MonetCompoundDataType getCompoundDataType(MonetDataType type) {
        switch (type) {
        case BOOLEAN:
        case DECIMAL: // TODO
        case DOUBLE:
        case INT:
        case REAL:
        case TIME:
        case TIMESTAMP:
        case DATE:
        case MONTH_INTERVAL:
        case SECOND_INTERVAL:
        case BLOB:
            return MonetCompoundDataType.create(type);
        case STRING: // TODO
            if (Randomly.getBoolean()) {
                return MonetCompoundDataType.create(type);
            } else {
                return MonetCompoundDataType.create(type, (int) Randomly.getNotCachedInteger(1, 1000));
            }
        default:
            throw new AssertionError(type);
        }

    }

    private enum AnyTypeExpression {
        CAST, FUNCTION, CONSTANT, CASE, COALESCE, SUBQUERY
    }

    private MonetExpression generateAnyTypeExpression(int depth, Randomly r, MonetDataType type) {
        AnyTypeExpression option;
        List<AnyTypeExpression> validOptions = new ArrayList<>(Arrays.asList(AnyTypeExpression.values()));
        option = Randomly.fromList(validOptions);

        switch (option) {
        case CAST:
            return new MonetCastOperation(generateExpression(depth + 1), getCompoundDataType(type));
        case FUNCTION:
            if (type == MonetDataType.STRING && Randomly.getBooleanWithSmallProbability())
                return generateConcat(depth);
            return generateFunction(depth + 1, type);
        case CONSTANT:
            return generateConstant(r, type);
        case SUBQUERY:
            MonetSelect select = MonetRandomQueryGenerator.createRandomSingleColumnQuery(depth + 1, type, globalState, false, false, this.allowParameters);
            return new MonetSelect.MonetSubquery(select, null);
        case CASE:
            MonetDataType tp = Randomly.fromOptions(MonetDataType.values());
            int noptions = Randomly.smallNumber() + 1;
            return new MonetCaseOperation(Randomly.getBoolean() ? generateExpression(depth + 1, tp) : null,
                generateExpressions(depth + 1, noptions, tp), generateExpressions(depth + 1, noptions, type),
                Randomly.getBoolean() ? generateExpression(depth + 1, type) : null);
        case COALESCE:
            int options = Randomly.smallNumber() + 2;
            return new MonetCoalesceOperation(generateExpressions(depth + 1, options, type));
        default:
            throw new AssertionError();
        }
    }

    private MonetExpression generateConcat(int depth) {
        MonetExpression left = generateExpression(depth + 1, MonetDataType.STRING);
        MonetExpression right = generateExpression(depth + 1);
        return new MonetConcatOperation(left, right);
    }

    private enum BitExpression {
        BINARY_OPERATION
    };

    private MonetExpression generateBitExpression(int depth) {
        BitExpression option;
        option = Randomly.fromOptions(BitExpression.values());
        switch (option) {
        case BINARY_OPERATION:
            return new MonetBinaryBitOperation(MonetBinaryBitOperator.getRandom(),
                    generateExpression(depth + 1, MonetDataType.INT),
                    generateExpression(depth + 1, MonetDataType.INT));
        default:
            throw new AssertionError();
        }
    }

    private enum IntExpression {
        UNARY_OPERATION, FUNCTION, CAST, BINARY_ARITHMETIC_EXPRESSION, CASE, COALESCE, SUBQUERY
    }

    private MonetExpression generateIntExpression(int depth) {
        IntExpression option;
        option = Randomly.fromOptions(IntExpression.values());
        switch (option) {
        case CAST:
            return new MonetCastOperation(generateExpression(depth + 1), getCompoundDataType(MonetDataType.INT));
        case UNARY_OPERATION:
            MonetExpression intExpression = generateExpression(depth + 1, MonetDataType.INT);
            return new MonetPrefixOperation(intExpression,
                    Randomly.getBoolean() ? PrefixOperator.UNARY_PLUS : PrefixOperator.UNARY_MINUS);
        case FUNCTION:
            return generateFunction(depth + 1, MonetDataType.INT);
        case BINARY_ARITHMETIC_EXPRESSION:
            return new MonetBinaryArithmeticOperation(generateExpression(depth + 1, MonetDataType.INT),
                    generateExpression(depth + 1, MonetDataType.INT), MonetBinaryOperator.getRandom());
        case SUBQUERY:
            MonetSelect select = MonetRandomQueryGenerator.createRandomSingleColumnQuery(depth + 1, MonetDataType.INT, globalState, false, false, this.allowParameters);
            return new MonetSelect.MonetSubquery(select, null);
        case CASE:
            MonetDataType tp = Randomly.fromOptions(MonetDataType.values());
            int noptions = Randomly.smallNumber() + 1;
            return new MonetCaseOperation(Randomly.getBoolean() ? generateExpression(depth + 1, tp) : null,
                generateExpressions(depth + 1, noptions, tp), generateExpressions(depth + 1, noptions, MonetDataType.INT),
                Randomly.getBoolean() ? generateExpression(depth + 1, MonetDataType.INT) : null);
        case COALESCE:
            int options = Randomly.smallNumber() + 2;
            return new MonetCoalesceOperation(generateExpressions(depth + 1, options, MonetDataType.INT));
        default:
            throw new AssertionError();
        }
    }

    private MonetExpression createColumnOfType(MonetDataType type) {
        List<MonetColumn> columns = filterColumns(type);
        MonetColumn fromList = Randomly.fromList(columns);
        MonetConstant value = rw == null ? null : rw.getValues().get(fromList);
        return MonetColumnValue.create(fromList, value);
    }

    final List<MonetColumn> filterColumns(MonetDataType type) {
        if (columns == null) {
            return Collections.emptyList();
        } else {
            return columns.stream().filter(c -> c.getType() == type).collect(Collectors.toList());
        }
    }

    public MonetExpression generateExpressionWithExpectedResult(MonetDataType type) {
        //this.expectedResult = true;
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState).setColumns(columns)
                .setRowValue(rw);
        MonetExpression expr;
        do {
            expr = gen.generateExpression(type);
        } while (expr.getExpectedValue() == null);
        return expr;
    }

    public static MonetExpression generateConstant(Randomly r, MonetDataType type) {
        if (Randomly.getBooleanWithSmallProbability()) {
            return MonetConstant.createNullConstant();
        }
        switch (type) {
        case INT:
            long val = r.getInteger();
            if (val == (long) Byte.MIN_VALUE || val == (long) Short.MIN_VALUE || val == (long) Integer.MIN_VALUE || val == Long.MIN_VALUE) { /* Monet uses MIN_VALUEs as NULL */
                val++;
            }
            if (Randomly.getBooleanWithSmallProbability()) {
                return MonetConstant.createTextConstant(String.valueOf(val));
            } else {
                return MonetConstant.createIntConstant(val);
            }
        case BOOLEAN:
            if (Randomly.getBooleanWithSmallProbability() && !MonetProvider.generateOnlyKnown) {
                return MonetConstant
                        .createTextConstant(Randomly.fromOptions("TRUE", "FALSE", "0", "1"));
            } else {
                return MonetConstant.createBooleanConstant(Randomly.getBoolean());
            }
        case STRING:
            return MonetConstant.createTextConstant(r.getString());
        case DECIMAL:
            return MonetConstant.createDecimalConstant(r.getRandomBigDecimal());
        case REAL:
            return MonetConstant.createFloatConstant((float) r.getDouble());
        case DOUBLE:
            return MonetConstant.createDoubleConstant(r.getDouble());
        case TIME:
            return MonetConstant.createTimeConstant(r.getIntegerBounded(86400000));
        case TIMESTAMP:
            return MonetConstant.createTimestampConstant(r.getIntegerBounded(2147483647)); /* TODO Java doesn't have Random.nextLong(long max) why?? */
        case DATE:
            return MonetConstant.createDateConstant(r.getIntegerBounded(2147483647));
        case MONTH_INTERVAL:
            return MonetConstant.createMonthIntervalConstant(r.getIntegerBounded(-2147483647, 2147483647));
        case SECOND_INTERVAL:
            return MonetConstant.createSecondIntervalConstant(r.getIntegerBounded(-2147483647, 2147483647));
        case BLOB:
            return MonetConstant.createBlobConstant(r.getString(Randomly.StringGenerationStrategy.HEX));
        default:
            throw new AssertionError(type);
        }
    }

    public static MonetExpression generateExpression(MonetGlobalState globalState, List<MonetColumn> columns,
            MonetDataType type) {
        return new MonetExpressionGenerator(globalState).setColumns(columns).generateExpression(0, type);
    }

    public static MonetExpression generateExpression(MonetGlobalState globalState, List<MonetColumn> columns) {
        return new MonetExpressionGenerator(globalState).setColumns(columns).generateExpression(0);

    }

    public List<MonetExpression> generateExpressions(int nr) {
        List<MonetExpression> expressions = new ArrayList<>();
        for (int i = 0; i < nr; i++) {
            expressions.add(generateExpression(0));
        }
        return expressions;
    }

    public MonetExpression generateExpression(MonetDataType dataType) {
        return generateExpression(0, dataType);
    }

    public MonetExpressionGenerator setGlobalState(MonetGlobalState globalState) {
        //this.globalState = globalState;
        return this;
    }

    public MonetExpression generateHavingClause() {
        this.allowAggregateFunctions = true;
        MonetExpression expression = generateExpression(MonetDataType.BOOLEAN);
        this.allowAggregateFunctions = false;
        return expression;
    }

    public MonetExpression generateAggregate() {
        if (Randomly.getBooleanWithRatherLowProbability()) {
            return new MonetAggregate(new ArrayList<MonetExpression>(), MonetAggregateFunction.COUNT_ALL, Randomly.getBoolean());
        } else {
            return getAggregate(MonetDataType.getRandomType());
        }
    }

    private MonetExpression getAggregate(MonetDataType dataType) {
        List<MonetAggregateFunction> aggregates = MonetAggregateFunction.getAggregates(dataType);
        MonetAggregateFunction agg = Randomly.fromList(aggregates);
        return generateArgsForAggregate(dataType, agg);
    }

    public MonetAggregate generateArgsForAggregate(MonetDataType dataType, MonetAggregateFunction agg) {
        return new MonetAggregate(generateExpressions(0, agg.getNrArgs(), dataType), agg, Randomly.getBoolean());
    }

    public MonetExpressionGenerator allowAggregates(boolean value) {
        allowAggregateFunctions = value;
        return this;
    }

    @Override
    public MonetExpression generatePredicate() {
        return generateExpression(0);
    }

    @Override
    public MonetExpression negatePredicate(MonetExpression predicate) {
        return new MonetPrefixOperation(predicate, MonetPrefixOperation.PrefixOperator.NOT);
    }

    @Override
    public MonetExpression isNull(MonetExpression expr) {
        return new MonetPostfixOperation(expr, PostfixOperator.IS_NULL);
    }
}
