package sqlancer.monet.ast;

import sqlancer.Randomly;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;

public class MonetJoin implements MonetExpression {

    public enum MonetJoinType {
        INNER, LEFT, RIGHT, FULL, CROSS, NATURAL;

        public static MonetJoinType getRandom() {
            return Randomly.fromOptions(values());
        }

    }

    private final MonetTable table;
    private final MonetExpression onClause;
    private final MonetJoinType type;

    public MonetJoin(MonetTable table, MonetExpression onClause, MonetJoinType type) {
        this.table = table;
        this.onClause = onClause;
        this.type = type;
    }

    public MonetTable getTable() {
        return table;
    }

    public MonetExpression getOnClause() {
        return onClause;
    }

    public MonetJoinType getType() {
        return type;
    }

    @Override
    public MonetDataType getExpressionType() {
        throw new AssertionError();
    }

    @Override
    public MonetConstant getExpectedValue() {
        throw new AssertionError();
    }

}
