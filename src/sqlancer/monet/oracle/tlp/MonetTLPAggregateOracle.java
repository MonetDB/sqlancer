package sqlancer.monet.oracle.tlp;

import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import sqlancer.Main.StateLogger;
import sqlancer.MainOptions;
import sqlancer.ComparatorHelper;
import sqlancer.IgnoreMeException;
import sqlancer.QueryAdapter;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetAggregate;
import sqlancer.monet.ast.MonetAggregate.MonetAggregateFunction;
import sqlancer.monet.ast.MonetAlias;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetJoin;
import sqlancer.monet.ast.MonetPostfixOperation;
import sqlancer.monet.ast.MonetPostfixOperation.PostfixOperator;
import sqlancer.monet.ast.MonetPrefixOperation;
import sqlancer.monet.ast.MonetPrefixOperation.PrefixOperator;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.gen.MonetCommon;

public class MonetTLPAggregateOracle extends MonetTLPBase {

    private String firstResult;
    private String secondResult;
    private String originalQuery;
    private String metamorphicQuery;
    private StateLogger logger;
    private MainOptions options;

    public MonetTLPAggregateOracle(MonetGlobalState state) {
        super(state);
        MonetCommon.addGroupingErrors(errors);
        logger = state.getLogger();
        options = state.getOptions();
    }

    @Override
    public void check() throws SQLException {
        super.check();
        MonetAggregateFunction aggregateFunction = Randomly.fromOptions(MonetAggregateFunction.MAX,
                MonetAggregateFunction.MIN, MonetAggregateFunction.SUM,
                MonetAggregateFunction.COUNT);
        MonetAggregate aggregate = gen.generateArgsForAggregate(aggregateFunction.getRandomReturnType(),
                aggregateFunction);
        List<MonetExpression> fetchColumns = new ArrayList<>();
        fetchColumns.add(aggregate);
        while (Randomly.getBooleanWithRatherLowProbability()) {
            fetchColumns.add(gen.generateAggregate());
        }
        select.setFetchColumns(Arrays.asList(aggregate));
        if (Randomly.getBooleanWithRatherLowProbability()) {
            select.setOrderByExpressions(gen.generateOrderBy());
        }
        originalQuery = MonetVisitor.asString(select);
        firstResult = getAggregateResult(originalQuery);
        metamorphicQuery = createMetamorphicUnionQuery(select, aggregate, select.getFromList());
        secondResult = getAggregateResult(metamorphicQuery);

        if (options.logEachSelect()) {
            logger.writeCurrent(metamorphicQuery);
        }
        String queryFormatString = "-- %s;\n-- result: %s";
        String firstQueryString = String.format(queryFormatString, originalQuery, firstResult);
        String secondQueryString = String.format(queryFormatString, metamorphicQuery, secondResult);
        state.getState().getLocalState().log(String.format("%s\n%s", firstQueryString, secondQueryString));
        if (firstResult == null && secondResult != null || firstResult != null && secondResult == null
                || firstResult != null && !firstResult.contentEquals(secondResult)
                        && !ComparatorHelper.isEqualDouble(firstResult, secondResult)) {
            if (secondResult != null && secondResult.contains("Inf")) {
                throw new IgnoreMeException(); // FIXME: average computation
            }
            String assertionMessage = String.format("the results mismatch!\n%s\n%s", firstQueryString,
                    secondQueryString);
            throw new AssertionError(assertionMessage);
        }

    }

    private String createMetamorphicUnionQuery(MonetSelect select, MonetAggregate aggregate,
            List<MonetExpression> from) {
        String metamorphicQuery;
        MonetExpression whereClause = gen.generateExpression(MonetDataType.BOOLEAN);
        MonetExpression negatedClause = new MonetPrefixOperation(whereClause, PrefixOperator.NOT);
        MonetExpression notNullClause = new MonetPostfixOperation(whereClause, PostfixOperator.IS_NULL);
        List<MonetExpression> mappedAggregate = mapped(aggregate);
        MonetSelect leftSelect = getSelect(mappedAggregate, from, whereClause, select.getJoinClauses());
        MonetSelect middleSelect = getSelect(mappedAggregate, from, negatedClause, select.getJoinClauses());
        MonetSelect rightSelect = getSelect(mappedAggregate, from, notNullClause, select.getJoinClauses());
        metamorphicQuery = "SELECT " + getOuterAggregateFunction(aggregate).toString() + " FROM (";
        metamorphicQuery += MonetVisitor.asString(leftSelect) + " UNION ALL "
                + MonetVisitor.asString(middleSelect) + " UNION ALL " + MonetVisitor.asString(rightSelect);
        metamorphicQuery += ") as asdf";
        return metamorphicQuery;
    }

    private String getAggregateResult(String queryString) throws SQLException {
        // log TLP Aggregate SELECT queries on the current log file
        if (state.getOptions().logEachSelect()) {
            // TODO: refactor me
            state.getLogger().writeCurrent(queryString);
            try {
                state.getLogger().getCurrentFileWriter().flush();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        String resultString;
        QueryAdapter q = new QueryAdapter(queryString, errors);
        try (ResultSet result = q.executeAndGet(state)) {
            if (result == null) {
                throw new IgnoreMeException();
            }
            if (!result.next()) {
                resultString = null;
            } else {
                resultString = result.getString(1);
            }
        } catch (SQLException e) {
            throw new AssertionError(queryString, e);
        }
        return resultString;
    }

    private List<MonetExpression> mapped(MonetAggregate aggregate) {
        switch (aggregate.getFunction()) {
        case SUM:
        case COUNT:
        case MAX:
        case MIN:
            return aliasArgs(Arrays.asList(aggregate));
        // case AVG:
        //// List<MonetExpression> arg = Arrays.asList(new
        // MonetCast(aggregate.getExpr().get(0),
        // MonetDataType.DECIMAL.get()));
        // MonetAggregate sum = new MonetAggregate(MonetAggregateFunction.SUM,
        // aggregate.getExpr());
        // MonetCast count = new MonetCast(
        // new MonetAggregate(MonetAggregateFunction.COUNT, aggregate.getExpr()),
        // MonetDataType.DECIMAL.get());
        //// MonetBinaryArithmeticOperation avg = new
        // MonetBinaryArithmeticOperation(sum, count,
        // MonetBinaryArithmeticOperator.DIV);
        // return aliasArgs(Arrays.asList(sum, count));
        default:
            throw new AssertionError(aggregate.getFunction());
        }
    }

    private List<MonetExpression> aliasArgs(List<MonetExpression> originalAggregateArgs) {
        List<MonetExpression> args = new ArrayList<>();
        int i = 0;
        for (MonetExpression expr : originalAggregateArgs) {
            args.add(new MonetAlias(expr, "agg" + i++));
        }
        return args;
    }

    private String getOuterAggregateFunction(MonetAggregate aggregate) {
        switch (aggregate.getFunction()) {
        // case AVG:
        // return "SUM(agg0::DECIMAL)/SUM(agg1)::DECIMAL";
        case COUNT:
            return MonetAggregateFunction.SUM.toString() + "(agg0)";
        default:
            return aggregate.getFunction().toString() + "(agg0)";
        }
    }

    private MonetSelect getSelect(List<MonetExpression> aggregates, List<MonetExpression> from,
            MonetExpression whereClause, List<MonetJoin> joinList) {
        MonetSelect leftSelect = new MonetSelect();
        leftSelect.setFetchColumns(aggregates);
        leftSelect.setFromList(from);
        leftSelect.setWhereClause(whereClause);
        leftSelect.setJoinClauses(joinList);
        if (Randomly.getBooleanWithSmallProbability()) {
            leftSelect.setGroupByExpressions(gen.generateExpressions(Randomly.smallNumber() + 1));
        }
        return leftSelect;
    }

}
