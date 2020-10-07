package sqlancer.monet.gen;

import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.Query;
import sqlancer.common.query.QueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetExpression;

public final class MonetInsertGenerator {

    private MonetInsertGenerator() {
    }

    public static Query insert(MonetGlobalState globalState) {
        MonetTable table = globalState.getSchema().getRandomTable(t -> t.isInsertable());
        ExpectedErrors errors = new ExpectedErrors();
        errors.add("cannot insert into column");
        MonetCommon.addCommonExpressionErrors(errors);
        MonetCommon.addCommonInsertUpdateErrors(errors);
        errors.add("multiple assignments to same column");
        errors.add("violates not-null constraint");

        errors.add("cannot insert into view");
        StringBuilder sb = new StringBuilder();
        sb.append("INSERT INTO ");
        sb.append(table.getName());
        List<MonetColumn> columns = table.getRandomNonEmptyColumnSubset();
        sb.append("(");
        sb.append(columns.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
        sb.append(")");
        sb.append(" VALUES");

        if (globalState.getDmbsSpecificOptions().allowBulkInsert && Randomly.getBooleanWithSmallProbability()) {
            StringBuilder sbRowValue = new StringBuilder();
            sbRowValue.append("(");
            for (int i = 0; i < columns.size(); i++) {
                if (i != 0) {
                    sbRowValue.append(", ");
                }
                if (Randomly.getBoolean()) {
                    sbRowValue.append(MonetVisitor.asString(MonetExpressionGenerator
                        .generateConstant(globalState.getRandomly(), columns.get(i).getType())));
                } else {
                    sbRowValue.append(MonetVisitor.asString(new MonetExpressionGenerator(globalState)
                        .generateExpression(columns.get(i).getType())));
                }
            }
            sbRowValue.append(")");

            int n = (int) Randomly.getNotCachedInteger(1, 3);
            for (int i = 0; i < n; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                sb.append(sbRowValue);
            }
        } else {
            int n = Randomly.smallNumber() + 1;
            for (int i = 0; i < n; i++) {
                if (i != 0) {
                    sb.append(", ");
                }
                insertRow(globalState, sb, columns, n == 1);
            }
        }
        errors.add("duplicate key value violates unique constraint");
        errors.add("identity column defined as GENERATED ALWAYS");
        errors.add("out of range");
        errors.add("invalid input syntax");

        return new QueryAdapter(sb.toString(), errors);
    }

    private static void insertRow(MonetGlobalState globalState, StringBuilder sb, List<MonetColumn> columns,
            boolean canBeDefault) {
        sb.append("(");
        for (int i = 0; i < columns.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            if (!Randomly.getBooleanWithSmallProbability() || !canBeDefault) {
                MonetExpression generateConstant;
                if (Randomly.getBoolean()) {
                    generateConstant = MonetExpressionGenerator.generateConstant(globalState.getRandomly(),
                            columns.get(i).getType());
                } else {
                    generateConstant = new MonetExpressionGenerator(globalState)
                            .generateExpression(columns.get(i).getType());
                }
                sb.append(MonetVisitor.asString(generateConstant));
            } else {
                sb.append("DEFAULT");
            }
        }
        sb.append(")");
    }

}
