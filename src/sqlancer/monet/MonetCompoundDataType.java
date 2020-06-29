package sqlancer.monet;

import java.util.Optional;

import sqlancer.monet.MonetSchema.MonetDataType;

public final class MonetCompoundDataType {

    private final MonetDataType dataType;
    private final MonetCompoundDataType elemType;
    private final Integer size;

    private MonetCompoundDataType(MonetDataType dataType, MonetCompoundDataType elemType, Integer size) {
        this.dataType = dataType;
        this.elemType = elemType;
        this.size = size;
    }

    public MonetDataType getDataType() {
        return dataType;
    }

    public MonetCompoundDataType getElemType() {
        if (elemType == null) {
            throw new AssertionError();
        }
        return elemType;
    }

    public Optional<Integer> getSize() {
        if (size == null) {
            return Optional.empty();
        } else {
            return Optional.of(size);
        }
    }

    public static MonetCompoundDataType create(MonetDataType type, int size) {
        return new MonetCompoundDataType(type, null, size);
    }

    public static MonetCompoundDataType create(MonetDataType type) {
        return new MonetCompoundDataType(type, null, null);
    }
}
