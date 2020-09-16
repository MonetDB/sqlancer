package sqlancer.monet;

import java.util.List;

import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.ast.MonetAggregate;
import sqlancer.monet.ast.MonetAnyAllOperation;
import sqlancer.monet.ast.MonetAnyTypeOperation;
import sqlancer.monet.ast.MonetBetweenOperation;
import sqlancer.monet.ast.MonetBinaryLogicalOperation;
import sqlancer.monet.ast.MonetBinaryComparisonOperation;
import sqlancer.monet.ast.MonetCastOperation;
import sqlancer.monet.ast.MonetCaseOperation;
import sqlancer.monet.ast.MonetColumnValue;
import sqlancer.monet.ast.MonetConstant;
import sqlancer.monet.ast.MonetExpression;
import sqlancer.monet.ast.MonetExistsOperation;
import sqlancer.monet.ast.MonetFunction;
import sqlancer.monet.ast.MonetInOperation;
import sqlancer.monet.ast.MonetLikeOperation;
import sqlancer.monet.ast.MonetOrderByTerm;
import sqlancer.monet.ast.MonetPostfixOperation;
import sqlancer.monet.ast.MonetPostfixText;
import sqlancer.monet.ast.MonetPrefixOperation;
import sqlancer.monet.ast.MonetQuery.MonetSubquery;
import sqlancer.monet.ast.MonetSelect;
import sqlancer.monet.ast.MonetSelect.MonetFromTable;
import sqlancer.monet.ast.MonetSelect.MonetQueryCTE;
import sqlancer.monet.ast.MonetSet;
import sqlancer.monet.ast.MonetValues;
import sqlancer.monet.gen.MonetExpressionGenerator;

public interface MonetVisitor {

    void visit(MonetConstant constant);

    void visit(MonetPostfixOperation op);

    void visit(MonetColumnValue c);

    void visit(MonetPrefixOperation op);

    void visit(MonetSelect op);

    void visit(MonetSet op);

    void visit(MonetValues op);

    void visit(MonetOrderByTerm op);

    void visit(MonetFunction f);

    void visit(MonetCastOperation cast);

    void visit(MonetBetweenOperation op);

    void visit(MonetInOperation op);

    void visit(MonetCaseOperation op);

    void visit(MonetAnyTypeOperation op);

    void visit(MonetPostfixText op);

    void visit(MonetAggregate op);

    void visit(MonetFromTable from);

    void visit(MonetQueryCTE from);

    void visit(MonetSubquery subquery);

    void visit(MonetBinaryLogicalOperation op);

    void visit(MonetBinaryComparisonOperation op);

    void visit(MonetLikeOperation op);

    void visit(MonetExistsOperation op);

    void visit(MonetAnyAllOperation op);

    default void visit(MonetExpression expression) {
        if (expression instanceof MonetConstant) {
            visit((MonetConstant) expression);
        } else if (expression instanceof MonetPostfixOperation) {
            visit((MonetPostfixOperation) expression);
        } else if (expression instanceof MonetColumnValue) {
            visit((MonetColumnValue) expression);
        } else if (expression instanceof MonetPrefixOperation) {
            visit((MonetPrefixOperation) expression);
        } else if (expression instanceof MonetSelect) {
            visit((MonetSelect) expression);
        } else if (expression instanceof MonetSet) {
            visit((MonetSet) expression);
        } else if (expression instanceof MonetValues) {
            visit((MonetValues) expression);
        } else if (expression instanceof MonetOrderByTerm) {
            visit((MonetOrderByTerm) expression);
        } else if (expression instanceof MonetFunction) {
            visit((MonetFunction) expression);
        } else if (expression instanceof MonetCastOperation) {
            visit((MonetCastOperation) expression);
        } else if (expression instanceof MonetBetweenOperation) {
            visit((MonetBetweenOperation) expression);
        } else if (expression instanceof MonetInOperation) {
            visit((MonetInOperation) expression);
        } else if (expression instanceof MonetCaseOperation) {
            visit((MonetCaseOperation) expression);
        } else if (expression instanceof MonetAnyTypeOperation) {
            visit((MonetAnyTypeOperation) expression);
        } else if (expression instanceof MonetAggregate) {
            visit((MonetAggregate) expression);
        } else if (expression instanceof MonetPostfixText) {
            visit((MonetPostfixText) expression);
        } else if (expression instanceof MonetFromTable) {
            visit((MonetFromTable) expression);
        } else if (expression instanceof MonetQueryCTE) {
            visit((MonetQueryCTE) expression);
        } else if (expression instanceof MonetSubquery) {
            visit((MonetSubquery) expression);
        } else if (expression instanceof MonetBinaryComparisonOperation) {
            visit((MonetBinaryComparisonOperation) expression);
        } else if (expression instanceof MonetBinaryLogicalOperation) {
            visit((MonetBinaryLogicalOperation) expression);
        } else if (expression instanceof MonetLikeOperation) {
            visit((MonetLikeOperation) expression);
        } else if (expression instanceof MonetExistsOperation) {
            visit((MonetExistsOperation) expression);
        } else if (expression instanceof MonetAnyAllOperation) {
            visit((MonetAnyAllOperation) expression);
        } else {
            throw new AssertionError(expression);
        }
    }

    static String asString(MonetExpression expr) {
        MonetToStringVisitor visitor = new MonetToStringVisitor();
        visitor.visit(expr);
        return visitor.get();
    }

    static String asExpectedValues(MonetExpression expr) {
        MonetExpectedValueVisitor v = new MonetExpectedValueVisitor();
        v.visit(expr);
        return v.get();
    }

    static String getExpressionAsString(MonetGlobalState globalState, MonetDataType type,
            List<MonetColumn> columns) {
        MonetExpression expression = MonetExpressionGenerator.generateExpression(globalState, columns, type);
        MonetToStringVisitor visitor = new MonetToStringVisitor();
        visitor.visit(expression);
        return visitor.get();
    }

}
