package sqlancer.monet.gen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import sqlancer.Randomly;
import sqlancer.common.query.ExpectedErrors;
import sqlancer.common.query.SQLQueryAdapter;
import sqlancer.monet.MonetGlobalState;
import sqlancer.monet.MonetSchema.MonetColumn;
import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.MonetSchema.MonetTable;
import sqlancer.monet.MonetVisitor;
import sqlancer.monet.ast.MonetExpression;

public final class MonetMergeGenerator {

    private MonetMergeGenerator() {
    }

    private static void generateInsert(MonetGlobalState globalState, MonetExpressionGenerator gen, StringBuilder sb,
            MonetTable table) {
        List<MonetColumn> columns = table.getRandomNonEmptyColumnSubset();
        sb.append("(");
        sb.append(columns.stream().map(c -> c.getName()).collect(Collectors.joining(", ")));
        sb.append(")");
        sb.append(" VALUES (");
        for (int i = 0; i < columns.size(); i++) {
            MonetColumn column = columns.get(i);
            if (i != 0) {
                sb.append(", ");
            }
            if (!Randomly.getBooleanWithSmallProbability()) {
                MonetExpression generateConstant;
                if (Randomly.getBoolean()) {
                    generateConstant = MonetExpressionGenerator.generateConstant(globalState.getRandomly(),
                            column.getType());
                } else {
                    generateConstant = gen.generateExpression(column.getType());
                }
                sb.append(MonetVisitor.asString(generateConstant));
            } else {
                sb.append("DEFAULT");
            }
        }
        sb.append(")");
    }

    private static void generateUpdate(MonetGlobalState globalState, MonetExpressionGenerator gen, StringBuilder sb,
            List<MonetColumn> columns) {
        sb.append("UPDATE SET ");
        for (int i = 0; i < columns.size(); i++) {
            if (i != 0) {
                sb.append(", ");
            }
            MonetColumn column = columns.get(i);
            sb.append(column.getName());
            sb.append(" = ");
            if (!Randomly.getBoolean()) {
                sb.append(MonetVisitor.asString(
                        MonetExpressionGenerator.generateConstant(globalState.getRandomly(), column.getType())));
            } else if (Randomly.getBoolean()) {
                sb.append("DEFAULT");
            } else {
                sb.append("(");
                sb.append(MonetVisitor.asString(gen.generateExpression(column.getType())));
                sb.append(")");
            }
        }
    }

    public static SQLQueryAdapter create(MonetGlobalState globalState) {
        MonetTable table = globalState.getSchema().getRandomTable(t -> t.isInsertable());
        MonetTable joined = globalState.getSchema().getRandomTable();
        ExpectedErrors errors = new ExpectedErrors();
        MonetCommon.addCommonInsertUpdateErrors(errors);
        StringBuilder sb = new StringBuilder("MERGE INTO ");
        sb.append(table.getName());
        sb.append(" USING (");
        sb.append("SELECT * FROM ");
        sb.append(joined.getName());
        sb.append(") AS ");
        sb.append(joined.getName());
        sb.append(" ON ");
        MonetExpressionGenerator gen = new MonetExpressionGenerator(globalState);
        List<MonetColumn> array3 = new ArrayList<>(table.getColumns().size() + joined.getColumns().size());
        array3.addAll(table.getColumns());
        array3.addAll(joined.getColumns());
        sb.append(MonetVisitor.asString(gen.generateExpression(MonetDataType.BOOLEAN)));
        switch (Randomly.fromOptions(1, 2, 3)) {
        case 1:
            sb.append(" WHEN MATCHED THEN ");
            if (Randomly.getBoolean()) {
                generateUpdate(globalState, gen, sb, table.getColumns());
            } else {
                sb.append("DELETE");
            }
            break;
        case 2:
            sb.append(" WHEN NOT MATCHED THEN INSERT ");
            generateInsert(globalState, gen, sb, table);
            break;
        case 3:
            sb.append(" WHEN MATCHED THEN ");
            if (Randomly.getBoolean()) {
                generateUpdate(globalState, gen, sb, table.getColumns());
            } else {
                sb.append("DELETE");
            }
            sb.append(" WHEN NOT MATCHED THEN INSERT ");
            generateInsert(globalState, gen, sb, table);
            break;
        default:
            throw new AssertionError();
        }
        MonetCommon.addCommonExpressionErrors(errors);
        errors.add("Multiple rows in the input relation");
        errors.add("on both sides of the joining condition");
        return new SQLQueryAdapter(sb.toString(), errors);
    }

}
