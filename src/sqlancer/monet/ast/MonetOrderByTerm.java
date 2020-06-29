package sqlancer.monet.ast;

import sqlancer.Randomly;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetOrderByTerm implements MonetExpression {

    private final MonetOrder order;
    private final MonetExpression expr;
    private final MonetNullsFirstOrLast nullsorder;

    public enum MonetOrder {
        ASC, DESC;

        public static MonetOrder getRandomOrder() {
            return Randomly.fromOptions(MonetOrder.values());
        }
    }

    public enum MonetNullsFirstOrLast {
        NULLS_FIRST("NULLS FIRST"), NULLS_LAST("NULLS LAST");

        private final String text;

        private MonetNullsFirstOrLast(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }

        public static MonetNullsFirstOrLast getRandomNullsOrder() {
            return Randomly.fromOptions(MonetNullsFirstOrLast.values());
        }
    }

    public MonetOrderByTerm(MonetExpression expr, MonetOrder order, MonetNullsFirstOrLast nullsorder) {
        this.expr = expr;
        this.order = order;
        this.nullsorder = nullsorder;
    }

    public MonetOrder getOrder() {
        return order;
    }

    public MonetNullsFirstOrLast getNullsOrder() {
        return nullsorder;
    }

    public MonetExpression getExpr() {
        return expr;
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
