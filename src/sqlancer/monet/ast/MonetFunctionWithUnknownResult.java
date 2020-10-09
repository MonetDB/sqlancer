package sqlancer.monet.ast;

import java.util.ArrayList;
import java.util.List;

import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.gen.MonetExpressionGenerator;

public enum MonetFunctionWithUnknownResult {

    // String functions
    ASCII("ascii", MonetDataType.INT, MonetDataType.STRING),

    LENGTH1("\"length\"", MonetDataType.INT, MonetDataType.BLOB),
    LENGTH2("\"length\"", MonetDataType.INT, MonetDataType.STRING),
    CHAR_LENGTH("char_length", MonetDataType.INT, MonetDataType.STRING),
    OCTET_LENGTH1("octet_length", MonetDataType.INT, MonetDataType.BLOB),
    OCTET_LENGTH2("octet_length", MonetDataType.INT, MonetDataType.STRING),
    REPLACE("replace", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING), 
    REVERSE("reverse", MonetDataType.STRING, MonetDataType.STRING),
    UPPER("upper", MonetDataType.STRING, MonetDataType.STRING),
    LOWER("lower", MonetDataType.STRING, MonetDataType.STRING),
    /* These functions are dangerous if the input amount is large
    SPACE("space", MonetDataType.STRING, MonetDataType.INT),
    INDEX("\"index\"", MonetDataType.INT, MonetDataType.STRING, MonetDataType.BOOLEAN),
    RIGHT("\"right\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    LEFT("\"left\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    REPEAT("repeat", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    LPAD("lpad", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    LPAD2("lpad", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT, MonetDataType.STRING),
    RPAD("rpad", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    RPAD2("rpad", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT, MonetDataType.STRING),*/
    SUBSTR("substr", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    SUBSTR2("substr", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT, MonetDataType.INT),
    INSERT("\"insert\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT, MonetDataType.INT, MonetDataType.STRING),
    LOCATE("\"locate\"", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING),
    LOCATE3("\"locate\"", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    CHARINDEX("charindex", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING),
    CHARINDEX3("charindex", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    SPLITPART("splitpart", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    PATINDEX("patindex", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING),
    TRUNCATE("\"truncate\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    CONCAT("\"concat\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    TRIM("trim", MonetDataType.STRING, MonetDataType.STRING),
    TRIM2("trim", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    LTRIM("ltrim", MonetDataType.STRING, MonetDataType.STRING),
    LTRIM2("ltrim", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    RTRIM("rtrim", MonetDataType.STRING, MonetDataType.STRING),
    RTRIM2("rtrim", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),

    // round functions
    ROUND_REAL("round", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.INT),
    ROUND_DOUBLE("round", MonetDataType.DOUBLE, MonetDataType.DOUBLE, MonetDataType.INT),
    ROUND_DEC("round", MonetDataType.DECIMAL, MonetDataType.DECIMAL, MonetDataType.INT),
    SCALEUP("scale_up", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    SCALEUP_DOUBLE("scale_up", MonetDataType.DOUBLE, MonetDataType.DOUBLE, MonetDataType.DOUBLE),
    SCALEUP_DEC("scale_up", MonetDataType.DECIMAL, MonetDataType.DECIMAL, MonetDataType.INT),
    SCALEDOWN("scale_down", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    SCALEDOWN_DOUBLE("scale_down", MonetDataType.DOUBLE, MonetDataType.DOUBLE, MonetDataType.DOUBLE),
    SCALEDOWN_DEC("scale_down", MonetDataType.DECIMAL, MonetDataType.DECIMAL, MonetDataType.INT),

    // mathematical functions
    
    CBRT("cbrt", MonetDataType.REAL, MonetDataType.REAL),
    CEIL("ceil", MonetDataType.REAL, MonetDataType.REAL),
    CEILING("ceiling", MonetDataType.REAL, MonetDataType.REAL),
    DEGREES("degrees", MonetDataType.REAL, MonetDataType.REAL),
    RADIANS("radians", MonetDataType.REAL, MonetDataType.REAL),
    EXP("exp", MonetDataType.REAL, MonetDataType.REAL),
    LN("ln", MonetDataType.REAL, MonetDataType.REAL),
    LOG("log", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    LOG2("log", MonetDataType.REAL, MonetDataType.REAL),
    LOG10("log10", MonetDataType.REAL, MonetDataType.REAL),
    PI("pi", MonetDataType.REAL),
    POWER("power", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    FLOOR("floor", MonetDataType.REAL, MonetDataType.REAL),
    SQRT("sqrt", MonetDataType.REAL, MonetDataType.REAL),
 
    // trigonometric functions - complete
    ACOS("acos", MonetDataType.REAL, MonetDataType.REAL),
    ASIN("asin", MonetDataType.REAL, MonetDataType.REAL),
    ATAN("atan", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    COS("cos", MonetDataType.REAL, MonetDataType.REAL),
    COT("cot", MonetDataType.REAL, MonetDataType.REAL),
    SIN("sin", MonetDataType.REAL, MonetDataType.REAL),
    TAN("tan", MonetDataType.REAL, MonetDataType.REAL),

    // hyperbolic functions - complete
    SINH("sinh", MonetDataType.REAL, MonetDataType.REAL),
    COSH("cosh", MonetDataType.REAL, MonetDataType.REAL),
    TANH("tanh", MonetDataType.REAL, MonetDataType.REAL),

    //time functions
    LOCAL_TIMEZONE("local_timezone", MonetDataType.SECOND_INTERVAL),
    CURRENT_DATE("\"current_date\"", MonetDataType.DATE),

    DATE_SUB1("sql_sub", MonetDataType.DAY_INTERVAL, MonetDataType.DATE, MonetDataType.DATE),
    DATE_SUB2("sql_sub", MonetDataType.SECOND_INTERVAL, MonetDataType.TIME, MonetDataType.TIME),
    DATE_SUB3("sql_sub", MonetDataType.SECOND_INTERVAL, MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP),
    DATE_SUB4("sql_sub", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.SECOND_INTERVAL),
    DATE_SUB5("sql_sub", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.DAY_INTERVAL),
    DATE_SUB6("sql_sub", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.MONTH_INTERVAL),
    DATE_SUB7("sql_sub", MonetDataType.TIME, MonetDataType.TIME, MonetDataType.SECOND_INTERVAL),
    DATE_SUB8("sql_sub", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.SECOND_INTERVAL),
    DATE_SUB9("sql_sub", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.DAY_INTERVAL),
    DATE_SUB10("sql_sub", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.MONTH_INTERVAL),

    DATE_ADD1("sql_add", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.SECOND_INTERVAL),
    DATE_ADD2("sql_add", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.DAY_INTERVAL),
    DATE_ADD3("sql_add", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.MONTH_INTERVAL),
    DATE_ADD4("sql_add", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.SECOND_INTERVAL),
    DATE_ADD5("sql_add", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.DAY_INTERVAL),
    DATE_ADD6("sql_add", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.MONTH_INTERVAL),
    DATE_ADD7("sql_add", MonetDataType.TIME, MonetDataType.TIME, MonetDataType.SECOND_INTERVAL),

    DATE_CENTURY("\"century\"", MonetDataType.INT, MonetDataType.DATE),
    DATE_DECADE("\"decade\"", MonetDataType.INT, MonetDataType.DATE),
    DATE_YEAR("\"year\"", MonetDataType.INT, MonetDataType.DATE),
    DATE_QUARTER("\"quarter\"", MonetDataType.INT, MonetDataType.DATE),
    DATE_MONTH("\"month\"", MonetDataType.INT, MonetDataType.DATE),
    DATE_DAY("\"day\"", MonetDataType.INT, MonetDataType.DATE),
    DATE_DAYOFYEAR("dayofyear", MonetDataType.INT, MonetDataType.DATE),
    DATE_WEEKOFYEAR("weekofyear", MonetDataType.INT, MonetDataType.DATE),
    DATE_DAYOFWEEK("dayofweek", MonetDataType.INT, MonetDataType.DATE),
    DATE_DAYOFMONTH("dayofmonth", MonetDataType.INT, MonetDataType.DATE),
    DATE_WEEK("\"week\"", MonetDataType.INT, MonetDataType.DATE),
    DATE_EPOCH("\"epoch_ms\"", MonetDataType.INT, MonetDataType.DATE),
    TIME_HOUR("\"hour\"", MonetDataType.INT, MonetDataType.TIME),
    TIME_MINUTE("\"minute\"", MonetDataType.INT, MonetDataType.TIME),
    TIME_SECOND("\"second\"", MonetDataType.DECIMAL, MonetDataType.TIME),
    TIME_EPOCH("\"epoch_ms\"", MonetDataType.INT, MonetDataType.TIME),

    TIMESTAMP_CENTURY("\"century\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_DECADE("\"decade\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_YEAR("\"year\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_QUARTER("\"quarter\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_MONTH("\"month\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_DAY("\"day\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_DAYOFYEAR("dayofyear", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_WEEKOFYEAR("weekofyear", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_DAYOFWEEK("dayofweek", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_DAYOFMONTH("dayofmonth", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_WEEK("\"week\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_HOUR("\"hour\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_MINUTE("\"minute\"", MonetDataType.INT, MonetDataType.TIMESTAMP),
    TIMESTAMP_SECOND("\"second\"", MonetDataType.DECIMAL, MonetDataType.TIMESTAMP),
    TIMESTAMP_EPOCH("\"epoch_ms\"", MonetDataType.INT, MonetDataType.TIMESTAMP),

    MONINT_YEAR("\"year\"", MonetDataType.INT, MonetDataType.MONTH_INTERVAL),
    MONINT_MONTH("\"month\"", MonetDataType.INT, MonetDataType.MONTH_INTERVAL),
    DAYINT_DAY("\"day\"", MonetDataType.INT, MonetDataType.DAY_INTERVAL),
    DAYINT_HOUR("\"hour\"", MonetDataType.INT, MonetDataType.DAY_INTERVAL),
    DAYINT_MINUTE("\"minute\"", MonetDataType.INT, MonetDataType.DAY_INTERVAL),
    DAYINT_SECOND("\"second\"", MonetDataType.INT, MonetDataType.DAY_INTERVAL),
    DAYINT_EPOCH("\"epoch_ms\"", MonetDataType.INT, MonetDataType.DAY_INTERVAL),
    SECINT_DAY("\"day\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL),
    SECINT_HOUR("\"hour\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL),
    SECINT_MINUTE("\"minute\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL),
    SECINT_SECOND("\"second\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL),
    SECINT_EPOCH("\"epoch_ms\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL),

    INTERVAL_SUB1("sql_sub", MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL),
    INTERVAL_SUB2("sql_sub", MonetDataType.DAY_INTERVAL, MonetDataType.DAY_INTERVAL, MonetDataType.DAY_INTERVAL),
    INTERVAL_SUB3("sql_sub", MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL),
    INTERVAL_ADD1("sql_add", MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL),
    INTERVAL_ADD2("sql_add", MonetDataType.DAY_INTERVAL, MonetDataType.DAY_INTERVAL, MonetDataType.DAY_INTERVAL),
    INTERVAL_ADD3("sql_add", MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL),
    INTERVAL_NEG1("sql_neg", MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL),
    INTERVAL_NEG2("sql_neg", MonetDataType.DAY_INTERVAL, MonetDataType.DAY_INTERVAL),
    INTERVAL_NEG3("sql_neg", MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL),

    // UUID functions
    ISAUUID("\"isauuid\"", MonetDataType.BOOLEAN, MonetDataType.STRING);

    private String functionName;
    private MonetDataType returnType;
    private MonetDataType[] argTypes;

    MonetFunctionWithUnknownResult(String functionName, MonetDataType returnType, MonetDataType... indexType) {
        this.functionName = functionName;
        this.returnType = returnType;
        this.argTypes = indexType.clone();
    }

    public boolean isCompatibleWithReturnType(MonetDataType t) {
        return t == returnType;
    }

    public MonetExpression[] getArguments(MonetDataType returnType, MonetExpressionGenerator gen, int depth) {
        MonetExpression[] args = new MonetExpression[argTypes.length];
        for (int i = 0; i < args.length; i++) {
            args[i] = gen.generateExpression(depth, argTypes[i]);
        }
        return args;

    }

    public String getName() {
        return functionName;
    }

    public static List<MonetFunctionWithUnknownResult> getSupportedFunctions(MonetDataType type) {
        List<MonetFunctionWithUnknownResult> functions = new ArrayList<>();
        for (MonetFunctionWithUnknownResult func : values()) {
            if (func.isCompatibleWithReturnType(type)) {
                functions.add(func);
            }
        }
        return functions;
    }

}
