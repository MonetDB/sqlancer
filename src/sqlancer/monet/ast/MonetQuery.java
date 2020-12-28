package sqlancer.monet.ast;

import sqlancer.common.ast.SelectBase;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetQuery extends SelectBase<MonetExpression> implements MonetExpression {

    public static class MonetSubquery implements MonetExpression {
        private final MonetQuery s;
        private final String name;
        private final MonetDataType type;

        public MonetSubquery(MonetQuery s, String name, MonetDataType type) {
            this.s = s;
            this.name = name;
            this.type = type;
        }

        public MonetQuery getSelect() {
            return s;
        }

        public String getName() {
            return name;
        }

        @Override
        public MonetDataType getExpressionType() {
            return type;
        }
    }

}
