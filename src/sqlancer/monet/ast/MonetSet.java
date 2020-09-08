package sqlancer.monet.ast;

import sqlancer.Randomly;

public class MonetSet extends MonetQuery {

    private SetType type = SetType.UNION;
    private SetDistictOrAll distinctOrAll = SetDistictOrAll.ALL;
    private MonetQuery left;
    private MonetQuery right;

    public MonetSet(SetType type, SetDistictOrAll distinctOrAll, MonetQuery left, MonetQuery right) {
        this.type = type;
        this.distinctOrAll = distinctOrAll;
        this.left = left;
        this.right = right;
    }

    public enum SetType {
        EXCEPT, UNION, INTERSECT;

        public static SetType getRandom() {
            return Randomly.fromOptions(values());
        }
    }

    public void setSetType(SetType type) {
        this.type = type;
    }

    public SetType fetSetType() {
        return this.type;
    }

    public enum SetDistictOrAll {
        DISTINCT, ALL;

        public static SetDistictOrAll getRandom() {
            return Randomly.fromOptions(values());
        }
    }

    public void setSetDistictOrAll(SetDistictOrAll distinctOrAll) {
        this.distinctOrAll = distinctOrAll;
    }

    public SetDistictOrAll fetSetDistictOrAll() {
        return this.distinctOrAll;
    }

    public void setLeft(MonetQuery left) {
        this.left = left;
    }

    public MonetQuery getLeft() {
        return this.left;
    }

    public void setRight(MonetQuery right) {
        this.right = right;
    }

    public MonetQuery getRight() {
        return this.right;
    }

}
