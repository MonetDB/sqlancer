package sqlancer.monet.ast;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.math.BigDecimal;

import sqlancer.IgnoreMeException;
import sqlancer.monet.MonetSchema.MonetDataType;

public abstract class MonetConstant implements MonetExpression {

    public abstract String getTextRepresentation();

    public abstract String getUnquotedTextRepresentation();

    public static class BooleanConstant extends MonetConstant {

        private final boolean value;

        public BooleanConstant(boolean value) {
            this.value = value;
        }

        @Override
        public String getTextRepresentation() {
            return value ? "TRUE" : "FALSE";
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.BOOLEAN;
        }

        @Override
        public boolean asBoolean() {
            return value;
        }

        @Override
        public boolean isBoolean() {
            return true;
        }

        @Override
        public MonetConstant isEquals(MonetConstant rightVal) {
            if (rightVal.isNull()) {
                return MonetConstant.createNullConstant();
            } else if (rightVal.isBoolean()) {
                return MonetConstant.createBooleanConstant(value == rightVal.asBoolean());
            } else if (rightVal.isString()) {
                return MonetConstant
                        .createBooleanConstant(value == rightVal.cast(MonetDataType.BOOLEAN).asBoolean());
            } else {
                throw new AssertionError(rightVal);
            }
        }

        @Override
        protected MonetConstant isLessThan(MonetConstant rightVal) {
            if (rightVal.isNull()) {
                return MonetConstant.createNullConstant();
            } else if (rightVal.isString()) {
                return isLessThan(rightVal.cast(MonetDataType.BOOLEAN));
            } else {
                assert rightVal.isBoolean();
                return MonetConstant.createBooleanConstant((value ? 1 : 0) < (rightVal.asBoolean() ? 1 : 0));
            }
        }

        @Override
        public MonetConstant cast(MonetDataType type) {
            switch (type) {
            case BOOLEAN:
                return this;
            case TINYINT:
            case SMALLINT:
            case INT:
            case BIGINT:
            case HUGEINT:
                return MonetConstant.createIntConstant((value) ? 1 : 0, type);
            case STRING:
                return MonetConstant.createTextConstant(value ? "true" : "false");
            default:
                return null;
            }
        }

        @Override
        public String getUnquotedTextRepresentation() {
            return getTextRepresentation();
        }

    }

    public static class MonetNullConstant extends MonetConstant {

        @Override
        public String getTextRepresentation() {
            return "NULL";
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.STRING; /* be safe from NULL pointers! */
        }

        @Override
        public boolean isNull() {
            return true;
        }

        @Override
        public MonetConstant isEquals(MonetConstant rightVal) {
            return MonetConstant.createNullConstant();
        }

        @Override
        protected MonetConstant isLessThan(MonetConstant rightVal) {
            return MonetConstant.createNullConstant();
        }

        @Override
        public MonetConstant cast(MonetDataType type) {
            return MonetConstant.createNullConstant();
        }

        @Override
        public String getUnquotedTextRepresentation() {
            return getTextRepresentation();
        }

    }

    public static class StringConstant extends MonetConstant {

        private final String value;

        public StringConstant(String value) {
            this.value = value;
        }

        @Override
        public String getTextRepresentation() {
            return String.format("r'%s'", value.replace("'", "''"));
        }

        @Override
        public MonetConstant isEquals(MonetConstant rightVal) {
            if (rightVal.isNull()) {
                return MonetConstant.createNullConstant();
            } else if (rightVal.isInt()) {
                return cast(MonetDataType.INT).isEquals(rightVal.cast(MonetDataType.INT));
            } else if (rightVal.isBoolean()) {
                return cast(MonetDataType.BOOLEAN).isEquals(rightVal.cast(MonetDataType.BOOLEAN));
            } else if (rightVal.isString()) {
                return MonetConstant.createBooleanConstant(value.contentEquals(rightVal.asString()));
            } else {
                throw new AssertionError(rightVal);
            }
        }

        @Override
        protected MonetConstant isLessThan(MonetConstant rightVal) {
            if (rightVal.isNull()) {
                return MonetConstant.createNullConstant();
            } else if (rightVal.isInt()) {
                return cast(MonetDataType.INT).isLessThan(rightVal.cast(MonetDataType.INT));
            } else if (rightVal.isBoolean()) {
                return cast(MonetDataType.BOOLEAN).isLessThan(rightVal.cast(MonetDataType.BOOLEAN));
            } else if (rightVal.isString()) {
                return MonetConstant.createBooleanConstant(value.compareTo(rightVal.asString()) < 0);
            } else {
                throw new AssertionError(rightVal);
            }
        }

        @Override
        public MonetConstant cast(MonetDataType type) {
            if (type == MonetDataType.STRING) {
                return this;
            }
            String s = value.trim();
            switch (type) {
            case BOOLEAN:
                try {
                    return MonetConstant.createBooleanConstant(Long.parseLong(s) != 0);
                } catch (NumberFormatException e) {
                }
                switch (s.toUpperCase()) {
                case "TRUE":
                case "1":
                case "YES":
                    return MonetConstant.createTrue();
                case "FALSE":
                case "0":
                case "NO":
                default:
                    return MonetConstant.createFalse();
                }
            case TINYINT:
            case SMALLINT:
            case INT:
            case BIGINT:
            case HUGEINT:
                try {
                    return MonetConstant.createIntConstant(Long.parseLong(s), type);
                } catch (NumberFormatException e) {
                    return MonetConstant.createIntConstant(-1, type);
                }
            case STRING:
                return this;
            default:
                return null;
            }
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.STRING;
        }

        @Override
        public boolean isString() {
            return true;
        }

        @Override
        public String asString() {
            return value;
        }

        @Override
        public String getUnquotedTextRepresentation() {
            return value;
        }

    }

    public static class IntConstant extends MonetConstant {

        private final long val;

        private final MonetDataType type;

        public IntConstant(long val, MonetDataType type) {
            this.val = val;
            this.type = type;
        }

        @Override
        public String getTextRepresentation() {
            return String.valueOf(val);
        }

        @Override
        public MonetDataType getExpressionType() {
            return type;
        }

        @Override
        public long asInt() {
            return val;
        }

        @Override
        public boolean isInt() {
            return true;
        }

        @Override
        public MonetConstant isEquals(MonetConstant rightVal) {
            if (rightVal.isNull()) {
                return MonetConstant.createNullConstant();
            } else if (rightVal.isBoolean()) {
                return cast(MonetDataType.BOOLEAN).isEquals(rightVal);
            } else if (rightVal.isInt()) {
                return MonetConstant.createBooleanConstant(val == rightVal.asInt());
            } else if (rightVal.isString()) {
                return MonetConstant.createBooleanConstant(val == rightVal.cast(this.type).asInt());
            } else {
                throw new AssertionError(rightVal);
            }
        }

        @Override
        protected MonetConstant isLessThan(MonetConstant rightVal) {
            if (rightVal.isNull()) {
                return MonetConstant.createNullConstant();
            } else if (rightVal.isInt()) {
                return MonetConstant.createBooleanConstant(val < rightVal.asInt());
            } else if (rightVal.isBoolean()) {
                throw new AssertionError(rightVal);
            } else if (rightVal.isString()) {
                return MonetConstant.createBooleanConstant(val < rightVal.cast(this.type).asInt());
            } else {
                throw new IgnoreMeException();
            }

        }

        @Override
        public MonetConstant cast(MonetDataType type) {
            switch (type) {
            case BOOLEAN:
                return MonetConstant.createBooleanConstant(val != 0);
            case TINYINT:
            case SMALLINT:
            case INT:
            case BIGINT:
            case HUGEINT:
                return this;
            case STRING:
                return MonetConstant.createTextConstant(String.valueOf(val));
            default:
                return null;
            }
        }

        @Override
        public String getUnquotedTextRepresentation() {
            return getTextRepresentation();
        }

    }

    public static MonetConstant createNullConstant() {
        return new MonetNullConstant();
    }

    public String asString() {
        throw new UnsupportedOperationException(this.toString());
    }

    public boolean isString() {
        return false;
    }

    public static MonetConstant createIntConstant(long val, MonetDataType tp) {
        return new IntConstant(val, tp);
    }

    public static MonetConstant createBooleanConstant(boolean val) {
        return new BooleanConstant(val);
    }

    @Override
    public MonetConstant getExpectedValue() {
        return this;
    }

    public boolean isNull() {
        return false;
    }

    public boolean asBoolean() {
        throw new UnsupportedOperationException(this.toString());
    }

    public static MonetConstant createFalse() {
        return createBooleanConstant(false);
    }

    public static MonetConstant createTrue() {
        return createBooleanConstant(true);
    }

    public long asInt() {
        throw new UnsupportedOperationException(this.toString());
    }

    public boolean isBoolean() {
        return false;
    }

    public abstract MonetConstant isEquals(MonetConstant rightVal);

    public boolean isInt() {
        return false;
    }

    protected abstract MonetConstant isLessThan(MonetConstant rightVal);

    @Override
    public String toString() {
        return getTextRepresentation();
    }

    public abstract MonetConstant cast(MonetDataType type);

    public static MonetConstant createTextConstant(String string) {
        return new StringConstant(string);
    }

    public abstract static class MonetConstantBase extends MonetConstant {

        @Override
        public String getUnquotedTextRepresentation() {
            return null;
        }

        @Override
        public MonetConstant isEquals(MonetConstant rightVal) {
            return null;
        }

        @Override
        protected MonetConstant isLessThan(MonetConstant rightVal) {
            return null;
        }

        @Override
        public MonetConstant cast(MonetDataType type) {
            return null;
        }
    }

    public static class DecimalConstant extends MonetConstantBase {

        private final BigDecimal val;

        public DecimalConstant(BigDecimal val) {
            this.val = val;
        }

        @Override
        public String getTextRepresentation() {
            return String.valueOf(val);
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.DECIMAL;
        }

    }

    public static class FloatConstant extends MonetConstantBase {

        private final float val;

        public FloatConstant(float val) {
            this.val = val;
        }

        @Override
        public String getTextRepresentation() {
            if (Double.isFinite(val)) {
                return String.valueOf(val);
            } else {
                return "'" + val + "'";
            }
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.REAL;
        }

    }

    public static class DoubleConstant extends MonetConstantBase {

        private final double val;

        public DoubleConstant(double val) {
            this.val = val;
        }

        @Override
        public String getTextRepresentation() {
            if (Double.isFinite(val)) {
                return String.valueOf(val);
            } else {
                return "'" + val + "'";
            }
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.DOUBLE;
        }

    }

    public static MonetConstant createDecimalConstant(BigDecimal bigDecimal) {
        return new DecimalConstant(bigDecimal);
    }

    public static MonetConstant createFloatConstant(float val) {
        return new FloatConstant(val);
    }

    public static MonetConstant createDoubleConstant(double val) {
        return new DoubleConstant(val);
    }

    public static class TimeConstant extends MonetConstantBase {

        private final String textRepr;

        public TimeConstant(long val) {
            Timestamp timestamp = new Timestamp(val);
            SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss");
            this.textRepr = dateFormat.format(timestamp);
        }

        @Override
        public String getTextRepresentation() {
            return String.format("TIME '%s'", textRepr);
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.TIME;
        }

    }

    public static class DateConstant extends MonetConstantBase {

        private final String textRepr;

        public DateConstant(long val) {
            Timestamp timestamp = new Timestamp(val);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            this.textRepr = dateFormat.format(timestamp);
        }

        @Override
        public String getTextRepresentation() {
            return String.format("DATE '%s'", textRepr);
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.DATE;
        }

    }

    public static class TimestampConstant extends MonetConstantBase {

        private final String textRepr;

        public TimestampConstant(long val) {
            Timestamp timestamp = new Timestamp(val);
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            this.textRepr = dateFormat.format(timestamp);
        }

        @Override
        public String getTextRepresentation() {
            return String.format("TIMESTAMP '%s'", textRepr);
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.TIMESTAMP;
        }

    }

    public static MonetConstant createTimeConstant(long val) {
        return new TimeConstant(val);
    }

    public static MonetConstant createDateConstant(long val) {
        return new DateConstant(val);
    }

    public static MonetConstant createTimestampConstant(long val) {
        return new TimestampConstant(val);
    }

    public static class MonthIntervalConstant extends MonetConstantBase {

        private final String textRepr;

        public MonthIntervalConstant(long val) {
            this.textRepr = "INTERVAL '" + val + "' MONTH";
        }

        @Override
        public String getTextRepresentation() {
            return this.textRepr;
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.MONTH_INTERVAL;
        }

    }

    public static class SecondIntervalConstant extends MonetConstantBase {

        private String textRepr;

        private final MonetDataType type;

        public SecondIntervalConstant(long val, MonetDataType tp) {
            this.type = tp;
            this.textRepr = "INTERVAL '" + val;
            if (tp == MonetDataType.SECOND_INTERVAL) {
                this.textRepr += "' SECOND";
            } else {
                this.textRepr += "' DAY";
            }
        }

        @Override
        public String getTextRepresentation() {
            return this.textRepr;
        }

        @Override
        public MonetDataType getExpressionType() {
            return this.type;
        }

    }

    public static MonetConstant createMonthIntervalConstant(long val) {
        return new MonthIntervalConstant(val);
    }

    public static MonetConstant createSecondIntervalConstant(long val, MonetDataType tp) {
        return new SecondIntervalConstant(val, tp);
    }

    public static class BlobConstant extends MonetConstantBase {

        private final String textRepr;

        public BlobConstant(String val) {
            if (val.length() % 2 == 1) {
                this.textRepr = "BLOB '" + val + "0'";
            } else {
                this.textRepr = "BLOB '" + val + "'";
            }
        }

        @Override
        public String getTextRepresentation() {
            return this.textRepr;
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.BLOB;
        }

    }

    public static MonetConstant createBlobConstant(String val) {
        return new BlobConstant(val);
    }

    public static class UUIDConstant extends MonetConstantBase {

        private final String textRepr;

        public UUIDConstant(String val) {
            StringBuilder sb = new StringBuilder(64);
            sb.append("UUID '")
            .append(val.substring(0, 8))
            .append("-")
            .append(val.substring(8, 12))
            .append("-")
            .append(val.substring(12, 16))
            .append("-")
            .append(val.substring(16, 20))
            .append("-")
            .append(val.substring(20, 32))
            .append("'");
            this.textRepr = sb.toString();
        }

        @Override
        public String getTextRepresentation() {
            return this.textRepr;
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.UUID;
        }

    }

    public static MonetConstant createUUIDConstant(String val) {
        return new UUIDConstant(val);
    }

    public static class MonetParameterConstant extends MonetConstantBase {

        @Override
        public String getTextRepresentation() {
            return "?";
        }

        @Override
        public MonetDataType getExpressionType() {
            return MonetDataType.STRING; /* be safe from NULL pointers! */
        }

        @Override
        public boolean isNull() {
            return false;
        }

        @Override
        public String getUnquotedTextRepresentation() {
            return getTextRepresentation();
        }

    }

    public static MonetConstant createParameterConstant() {
        return new MonetParameterConstant();
    }

}
