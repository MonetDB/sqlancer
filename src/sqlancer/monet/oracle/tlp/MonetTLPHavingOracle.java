package sqlancer.monet.oracle.tlp;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import sqlancer.ComparatorHelper;
import sqlancer.Randomly;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.gen.MonetCommon;

public class MonetTLPHavingOracle extends MonetTLPBase {

    public MonetTLPHavingOracle(MonetGlobalState state) {
        super(state);
        MonetCommon.addGroupingErrors(errors);
    }

    @Override
    public void check() throws SQLException {
        super.check();
        havingCheck();
    }

    protected void havingCheck() throws SQLException {
        if (Randomly.getBoolean()) {
            select.setWhereClause(gen.generateExpression(MonetDataType.BOOLEAN));
        }
        select.setGroupByExpressions(gen.generateExpressions(Randomly.smallNumber() + 1));
        select.setHavingClause(null);
        String originalQueryString = MonetVisitor.asString(select);
        List<String> resultSet = ComparatorHelper.getResultSetFirstColumnAsString(originalQueryString, errors, state);

        boolean orderBy = Randomly.getBoolean();
        if (orderBy) {
            select.setOrderByExpressions(gen.generateOrderBy());
        }
        select.setHavingClause(predicate);
        String firstQueryString = MonetVisitor.asString(select);
        select.setHavingClause(negatedPredicate);
        String secondQueryString = MonetVisitor.asString(select);
        select.setHavingClause(isNullPredicate);
        String thirdQueryString = MonetVisitor.asString(select);
        List<String> combinedString = new ArrayList<>();
        List<String> secondResultSet = ComparatorHelper.getCombinedResultSet(firstQueryString, secondQueryString,
                thirdQueryString, combinedString, !orderBy, state, errors);
        ComparatorHelper.assumeResultSetsAreEqual(resultSet, secondResultSet, originalQueryString, combinedString,
                state);
    }

    @Override
    protected MonetExpression generatePredicate() {
        return gen.generateHavingClause();
    }

    @Override
    List<MonetExpression> generateFetchColumns() {
        List<MonetExpression> expressions = gen.allowAggregates(true).generateExpressions(Randomly.smallNumber() + 1);
        gen.allowAggregates(false);
        return expressions;
    }

}
