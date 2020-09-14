package sqlancer.monet;

import java.util.Arrays;
import java.util.Optional;

import sqlancer.Randomly;
import sqlancer.common.visitor.BinaryOperation;
import sqlancer.common.visitor.ToStringVisitor;
import sqlancer.monet.ast.MonetAggregate;
import sqlancer.monet.ast.MonetAggregate.MonetAggregateFunction;
import sqlancer.monet.ast.MonetAnyAllOperation;
import sqlancer.monet.ast.MonetAnyTypeOperation;
import sqlancer.monet.ast.MonetBetweenOperation;
import sqlancer.monet.ast.MonetBinaryComparisonOperation;
import sqlancer.monet.ast.MonetBinaryLogicalOperation;
import sqlancer.monet.ast.MonetCaseOperation;
import sqlancer.monet.ast.MonetCastOperation;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExistsOperation;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetFunction;
import sqlancer.monet.ast.MonetInOperation;
import sqlancer.monet.ast.MonetJoin;
import sqlancer.monet.ast.MonetJoin.MonetJoinType;
import sqlancer.monet.ast.MonetLikeOperation;
import sqlancer.monet.ast.MonetOrderByTerm;
import sqlancer.monet.ast.MonetPostfixOperation;
import sqlancer.monet.ast.MonetPostfixText;
import sqlancer.monet.ast.MonetPrefixOperation;
import sqlancer.monet.ast.MonetQuery.MonetSubquery;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSet;
import sqlancer.monet.MonetSchema.MonetDataType;

public final class MonetToStringVisitor extends ToStringVisitor<MonetExpression> implements MonetVisitor {

    @Override
    public void visitSpecific(MonetExpression expr) {
        MonetVisitor.super.visit(expr);
    }

    @Override
    public void visit(MonetConstant constant) {
        sb.append(constant.getTextRepresentation());
    }

    @Override
    public String get() {
        return sb.toString();
    }

    @Override
    public void visit(MonetPostfixOperation op) {
        sb.append("(");
        visit(op.getExpression());
        sb.append(")");
        sb.append(" ");
        sb.append(op.getOperatorTextRepresentation());
    }

    @Override
    public void visit(MonetColumnValue c) {
        sb.append(c.getColumn().getFullQualifiedName());
    }

    @Override
    public void visit(MonetPrefixOperation op) {
        sb.append(op.getTextRepresentation());
        sb.append(" (");
        visit(op.getExpression());
        sb.append(")");
    }

    @Override
    public void visit(MonetFromTable from) {
        sb.append(from.getTable().getName());
        /*if (!from.isOnly() && Randomly.getBoolean()) {
            sb.append(".*");
        }*/
    }

    @Override
    public void visit(MonetSubquery subquery) {
        sb.append("(");
        visit(subquery.getSelect());
        sb.append(")");
        if (subquery.getName() != null) {
            sb.append(" AS ");
            sb.append(subquery.getName());
        }
    }

    @Override
    public void visit(MonetSelect s) {
        sb.append("SELECT ");
        switch (s.getSelectOption()) {
        case DISTINCT:
            sb.append("DISTINCT ");
            break;
        case ALL:
            sb.append(Randomly.fromOptions("ALL ", ""));
            break;
        default:
            throw new AssertionError();
        }
        if (s.getFetchColumns() == null) {
            sb.append(".*");
        } else {
            visit(s.getFetchColumns());
        }
        sb.append(" FROM ");
        visit(s.getFromList());

        for (MonetJoin j : s.getJoinClauses()) {
            sb.append(" ");
            switch (j.getType()) {
            case INNER:
                if (Randomly.getBoolean()) {
                    sb.append("INNER ");
                }
                sb.append("JOIN");
                break;
            case LEFT:
                sb.append("LEFT OUTER JOIN");
                break;
            case RIGHT:
                sb.append("RIGHT OUTER JOIN");
                break;
            case FULL:
                sb.append("FULL OUTER JOIN");
                break;
            case CROSS:
                sb.append("CROSS JOIN");
                break;
            case NATURAL:
                sb.append("NATURAL JOIN");
                break;
            default:
                throw new AssertionError(j.getType());
            }
            sb.append(" ");
            visit(j.getTableReference());
            if (j.getType() != MonetJoinType.CROSS && j.getType() != MonetJoinType.NATURAL) {
                sb.append(" ON ");
                visit(j.getOnClause());
            }
        }

        if (s.getWhereClause() != null) {
            sb.append(" WHERE ");
            visit(s.getWhereClause());
        }
        if (s.getGroupByExpressions().size() > 0) {
            sb.append(" GROUP BY ");
            visit(s.getGroupByExpressions());
        }
        if (s.getHavingClause() != null) {
            sb.append(" HAVING ");
            visit(s.getHavingClause());
        }
        if (!s.getOrderByExpressions().isEmpty()) {
            sb.append(" ORDER BY ");
            visit(s.getOrderByExpressions());
        }
        if (s.getLimitClause() != null) {
            sb.append(" LIMIT ");
            visit(s.getLimitClause());
        }
        if (s.getOffsetClause() != null) {
            sb.append(" OFFSET ");
            visit(s.getOffsetClause());
        }
    }

    @Override
    public void visit(MonetSet query) {
        sb.append("(");
        visit(query.getLeft());
        sb.append(")");
        switch (query.fetSetType()) {
        case INTERSECT:
            sb.append(" INTERSECT ");
            break;
        case EXCEPT:
            sb.append(" EXCEPT ");
            break;
        case UNION:
            sb.append(" UNION ");
            break;
        default:
            throw new AssertionError();
        }
        switch (query.fetSetDistictOrAll()) {
        case DISTINCT:
            sb.append("DISTINCT ");
            break;
        case ALL:
            sb.append(Randomly.fromOptions("ALL ", ""));
            break;
        default:
            throw new AssertionError();
        }
        sb.append("(");
        visit(query.getRight());
        sb.append(")");
    }

    @Override
    public void visit(MonetOrderByTerm op) {
        visit(op.getExpr());
        sb.append(" ");
        sb.append(op.getOrder());
        sb.append(" ");
        sb.append(op.getNullsOrder().getText());
    }

    @Override
    public void visit(MonetFunction f) {
        sb.append(f.getFunctionName());
        sb.append("(");
        int i = 0;
        for (MonetExpression arg : f.getArguments()) {
            if (i++ != 0) {
                sb.append(", ");
            }
            visit(arg);
        }
        sb.append(")");
    }

    @Override
    public void visit(MonetCastOperation cast) {
        sb.append("CAST(");
        visit(cast.getExpression());
        sb.append(" AS ");
        appendType(cast);
        sb.append(")");
    }

    private void appendType(MonetCastOperation cast) {
        MonetCompoundDataType compoundType = cast.getCompoundType();
        switch (compoundType.getDataType()) {
        case BOOLEAN:
            sb.append("BOOLEAN");
            break;
        case INT: // TODO support also other int types
            sb.append("INT");
            break;
        case STRING:
            // TODO: append TEXT, CHAR
            sb.append(Randomly.fromOptions("STRING"));
            break;
        case REAL:
            sb.append("REAL");
            break;
        case DECIMAL:
            sb.append("DECIMAL");
            break;
        case DOUBLE:
            sb.append("DOUBLE");
            break;
        case TIME:
            sb.append("TIME");
            break;
        case TIMESTAMP:
            sb.append("TIMESTAMP");
            break;
        case DATE:
            sb.append("DATE");
            break;
        case MONTH_INTERVAL:
            sb.append("INTERVAL MONTH");
            break;
        case SECOND_INTERVAL:
            sb.append("INTERVAL SECOND");
            break;
        case BLOB:
            sb.append("BLOB");
            break;
        default:
            throw new AssertionError(cast.getType());
        }
        Optional<Integer> size = compoundType.getSize();
        MonetDataType[] exclude = new MonetDataType[]{MonetDataType.INT,MonetDataType.TIME,MonetDataType.TIMESTAMP,MonetDataType.DATE,MonetDataType.MONTH_INTERVAL,MonetDataType.SECOND_INTERVAL};
        if (size.isPresent() && !Arrays.stream(exclude).allMatch(t -> t.equals(compoundType.getDataType()))) {
            sb.append("(");
            sb.append(size.get());
            sb.append(")");
        }
    }

    @Override
    public void visit(MonetBetweenOperation op) {
        sb.append("(");
        visit(op.getExpr());
        sb.append(")");
        if (op.isNot()) {
            sb.append(" NOT");
        }
        sb.append(" BETWEEN ");
        if (op.isSymmetric()) {
            sb.append("SYMMETRIC ");
        } else {
            sb.append("ASYMMETRIC ");
        }
        sb.append("(");
        visit(op.getLeft());
        sb.append(") AND (");
        visit(op.getRight());
        sb.append(")");
    }

    @Override
    public void visit(MonetInOperation op) {
        sb.append("(");
        visit(op.getExpr());
        sb.append(")");
        if (!op.isTrue()) {
            sb.append(" NOT");
        }
        sb.append(" IN (");
        visit(op.getListElements());
        sb.append(")");
    }

    @Override
    public void visit(MonetAnyAllOperation op) {
        visit(op.getExpr());
        sb.append(" ");
        sb.append(op.getComparison().getTextRepresentation());
        if (op.isAny()) {
            sb.append(" ANY(");
        } else {
            sb.append(" ALL(");
        }
        visit(op.getSelect());
        sb.append(")");
    }

    @Override
    public void visit(MonetExistsOperation op) {
        if (!op.isExists()) {
            sb.append("NOT ");
        }
        sb.append("EXISTS (");
        visit(op.getSelect());
        sb.append(")");
    }

    @Override
    public void visit(MonetCaseOperation op) {
        sb.append("CASE");
        if (op.getSwitchCondition() != null) {
            sb.append(" ");
            visit(op.getSwitchCondition());
        }
        for (int i = 0; i < op.getConditions().size(); i++) {
            sb.append(" WHEN ");
            visit(op.getConditions().get(i));
            sb.append(" THEN ");
            visit(op.getExpressions().get(i));
        }
        if (op.getElseExpr() != null) {
            sb.append(" ELSE ");
            visit(op.getElseExpr());
        }
        sb.append(" END");
    }

    @Override
    public void visit(MonetAnyTypeOperation op) {
        sb.append(op.getFunction().getName());
        sb.append("(");
        int i = 0;
        for (MonetExpression arg : op.getArguments()) {
            if (i++ != 0) {
                sb.append(", ");
            }
            visit(arg);
        }
        sb.append(")");
    }

    @Override
    public void visit(MonetPostfixText op) {
        visit(op.getExpr());
        sb.append(op.getText());
    }

    @Override
    public void visit(MonetAggregate op) {
        sb.append(op.getFunction().getName());
        sb.append("(");

        if (op.getFunction() == MonetAggregateFunction.COUNT_ALL) {
            sb.append("*");
        } else {
            sb.append("ALL ");
            visit(op.getArgs());
        }
        sb.append(")");
    }

    @Override
    public void visit(MonetBinaryComparisonOperation op) {
        super.visit((BinaryOperation<MonetExpression>) op);
    }

    @Override
    public void visit(MonetBinaryLogicalOperation op) {
        super.visit((BinaryOperation<MonetExpression>) op);
    }

    @Override
    public void visit(MonetLikeOperation op) {
        super.visit((BinaryOperation<MonetExpression>) op);
    }

}
