package sqlancer.monet.ast;

import java.util.List;

public class MonetValues extends MonetQuery {

    private List<List<MonetExpression>> rowValues;

    public MonetValues(List<List<MonetExpression>> rowValues) {
        this.rowValues = rowValues;
    }

    public void setRowValues(List<List<MonetExpression>> rowValues) {
        this.rowValues = rowValues;
    }

    public List<List<MonetExpression>> getRowValues() {
        return this.rowValues;
    }

}
