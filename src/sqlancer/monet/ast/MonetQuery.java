package sqlancer.monet.ast;

import sqlancer.common.ast.SelectBase;
import sqlancer.monet.MonetSchema.MonetDataType;

public abstract class MonetQuery extends SelectBase<MonetExpression> implements MonetExpression {

    public static class MonetSubquery implements MonetExpression {
        private final MonetQuery s;
        private final String name;

        public MonetSubquery(MonetQuery s, String name) {
            this.s = s;
            this.name = name;
        }

        public MonetQuery getSelect() {
            return s;
        }

        public String getName() {
            return name;
        }

        @Override
        public MonetDataType getExpressionType() {
            return null;
        }
    }

}
