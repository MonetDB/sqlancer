package sqlancer.monet;

import sqlancer.monet.ast.MonetAggregate;
import sqlancer.monet.ast.MonetBetweenOperation;
import sqlancer.monet.ast.MonetBinaryComparisonOperation;
import sqlancer.monet.ast.MonetBinaryLogicalOperation;
import sqlancer.monet.ast.MonetCaseOperation;
import sqlancer.monet.ast.MonetCastOperation;
import sqlancer.monet.ast.MonetCoalesceOperation;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetFunction;
import sqlancer.monet.ast.MonetInOperation;
import sqlancer.monet.ast.MonetLikeOperation;
import sqlancer.monet.ast.MonetOrderByTerm;
import sqlancer.monet.ast.MonetPostfixOperation;
import sqlancer.monet.ast.MonetPostfixText;
import sqlancer.monet.ast.MonetPrefixOperation;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSelect.MonetSubquery;

public final class MonetExpectedValueVisitor implements MonetVisitor {

    private final StringBuilder sb = new StringBuilder();
    private static final int NR_TABS = 0;

    private void print(MonetExpression expr) {
        MonetToStringVisitor v = new MonetToStringVisitor();
        v.visit(expr);
        for (int i = 0; i < NR_TABS; i++) {
            sb.append("\t");
        }
        sb.append(v.get());
        sb.append(" -- ");
        sb.append(expr.getExpectedValue());
        sb.append("\n");
    }

    @Override
    public void visit(MonetConstant constant) {
        print(constant);
    }

    @Override
    public void visit(MonetPostfixOperation op) {
        print(op);
        visit(op.getExpression());
    }

    public String get() {
        return sb.toString();
    }

    @Override
    public void visit(MonetColumnValue c) {
        print(c);
    }

    @Override
    public void visit(MonetPrefixOperation op) {
        print(op);
        visit(op.getExpression());
    }

    @Override
    public void visit(MonetSelect op) {
        visit(op.getWhereClause());
    }

    @Override
    public void visit(MonetOrderByTerm op) {

    }

    @Override
    public void visit(MonetFunction f) {
        print(f);
        for (int i = 0; i < f.getArguments().length; i++) {
            visit(f.getArguments()[i]);
        }
    }

    @Override
    public void visit(MonetCastOperation cast) {
        print(cast);
        visit(cast.getExpression());
    }

    @Override
    public void visit(MonetBetweenOperation op) {
        print(op);
        visit(op.getExpr());
        visit(op.getLeft());
        visit(op.getRight());
    }

    @Override
    public void visit(MonetInOperation op) {
        print(op);
        visit(op.getExpr());
        for (MonetExpression right : op.getListElements()) {
            visit(right);
        }
    }

    @Override
    public void visit(MonetCaseOperation op) {
        if (op.getSwitchCondition() != null) {
            visit(op.getSwitchCondition());
        }
        for (int i = 0; i < op.getConditions().size(); i++) {
            visit(op.getConditions().get(i));
            visit(op.getExpressions().get(i));
        }
        if (op.getElseExpr() != null) {
            visit(op.getElseExpr());
        }
    }

    @Override
    public void visit(MonetCoalesceOperation op) {
        for (int i = 0; i < op.getConditions().size(); i++) {
            visit(op.getConditions().get(i));
        }
    }

    @Override
    public void visit(MonetPostfixText op) {
        print(op);
        visit(op.getExpr());
    }

    @Override
    public void visit(MonetAggregate op) {
        print(op);
        for (MonetExpression expr : op.getArgs()) {
            visit(expr);
        }
    }

    @Override
    public void visit(MonetFromTable from) {
        print(from);
    }

    @Override
    public void visit(MonetSubquery subquery) {
        print(subquery);
    }

    @Override
    public void visit(MonetBinaryLogicalOperation op) {
        print(op);
        visit(op.getLeft());
        visit(op.getRight());
    }

    @Override
    public void visit(MonetBinaryComparisonOperation op) {
        print(op);
        visit(op.getLeft());
        visit(op.getRight());
    }

    @Override
    public void visit(MonetLikeOperation op) {
        print(op);
        visit(op.getLeft());
        visit(op.getRight());
    }
}
