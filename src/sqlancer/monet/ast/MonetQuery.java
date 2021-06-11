package sqlancer.monet.ast;

import java.util.List;

import sqlancer.common.ast.SelectBase;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;

public class MonetQuery extends SelectBase<MonetExpression> implements MonetExpression {

    public static class MonetSubquery implements MonetExpression {
        private final MonetQuery s;
        private final String name;
        private final MonetDataType type;
        private final List<MonetColumn> cols;

        public MonetSubquery(MonetQuery s, String name, MonetDataType type, List<MonetColumn> cols) {
            this.s = s;
            this.name = name;
            this.type = type;
            this.cols = cols;
        }

        public MonetQuery getSelect() {
            return s;
        }

        public List<MonetColumn> getColumns() {
            return cols;
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
