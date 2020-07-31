package sqlancer.monet.ast;

import java.util.ArrayList;
import java.util.List;

import sqlancer.monet.MonetSchema.MonetDataType;
import sqlancer.monet.gen.MonetExpressionGenerator;

public enum MonetFunctionWithUnknownResult {

    // String functions
    ASCII("ascii", MonetDataType.INT, MonetDataType.STRING),

    /* These functions are dangerous if the input amount is large
    //SPACE("space", MonetDataType.STRING, MonetDataType.INT),*/

    LENGTH("\"length\"", MonetDataType.INT, MonetDataType.STRING),
    CHAR_LENGTH("char_length", MonetDataType.INT, MonetDataType.STRING),
    OCTET_LENGTH("octet_length", MonetDataType.INT, MonetDataType.STRING),

    REPLACE("replace", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING), 
    REVERSE("reverse", MonetDataType.STRING, MonetDataType.STRING),
    /* These functions are dangerous if the input amount is large
    RIGHT("\"right\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    LEFT("\"left\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),*/
    UPPER("upper", MonetDataType.STRING, MonetDataType.STRING),
    LOWER("lower", MonetDataType.STRING, MonetDataType.STRING),

    /* These functions are dangerous if the input amount is large
    REPEAT("repeat", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    LPAD("lpad", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    LPAD2("lpad", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT, MonetDataType.STRING),
    RPAD("rpad", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    RPAD2("rpad", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT, MonetDataType.STRING),*/
    SUBSTR("substr", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    SUBSTR2("substr", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT, MonetDataType.INT),

    INSERT("\"insert\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT, MonetDataType.INT, MonetDataType.STRING),

    //INDEX("\"index\"", MonetDataType.INT, MonetDataType.STRING, MonetDataType.BOOLEAN),
    LOCATE("\"locate\"", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING),
    LOCATE3("\"locate\"", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    CHARINDEX("charindex", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING),
    CHARINDEX3("charindex", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),

    SPLITPART("splitpart", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),

    PATINDEX("patindex", MonetDataType.INT, MonetDataType.STRING, MonetDataType.STRING),
    TRUNCATE("\"truncate\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.INT),
    CONCAT("\"concat\"", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    //CODE("code", MonetDataType.STRING, MonetDataType.INT),

    TRIM("trim", MonetDataType.STRING, MonetDataType.STRING),
    TRIM2("trim", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    LTRIM("ltrim", MonetDataType.STRING, MonetDataType.STRING),
    LTRIM2("ltrim", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    RTRIM("rtrim", MonetDataType.STRING, MonetDataType.STRING),
    RTRIM2("rtrim", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),

    // mathematical functions
    ROUND("round", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.INT),
    SIGN("sign", MonetDataType.REAL, MonetDataType.REAL),
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
    SCALE_UP("scale_up", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    SCALE_DOWN("scale_down", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),

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

    //TODO We need polimorfism!

    SQLMIN1("sql_min", MonetDataType.INT, MonetDataType.INT, MonetDataType.INT),
    SQLMIN2("sql_min", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    SQLMIN3("sql_min", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    SQLMIN4("sql_min", MonetDataType.DOUBLE, MonetDataType.DOUBLE, MonetDataType.DOUBLE),
    SQLMIN5("sql_min", MonetDataType.DECIMAL, MonetDataType.DECIMAL, MonetDataType.DECIMAL),
    SQLMIN6("sql_min", MonetDataType.TIME, MonetDataType.TIME, MonetDataType.TIME),
    SQLMIN7("sql_min", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP),
    SQLMIN8("sql_min", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.DATE),
    SQLMIN9("sql_min", MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL),
    SQLMIN10("sql_min", MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL),

    SQLMAX1("sql_max", MonetDataType.INT, MonetDataType.INT, MonetDataType.INT),
    SQLMAX2("sql_max", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    SQLMAX3("sql_max", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    SQLMAX4("sql_max", MonetDataType.DOUBLE, MonetDataType.DOUBLE, MonetDataType.DOUBLE),
    SQLMAX5("sql_max", MonetDataType.DECIMAL, MonetDataType.DECIMAL, MonetDataType.DECIMAL),
    SQLMAX6("sql_max", MonetDataType.TIME, MonetDataType.TIME, MonetDataType.TIME),
    SQLMAX7("sql_max", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP),
    SQLMAX8("sql_max", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.DATE),
    SQLMAX9("sql_max", MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL),
    SQLMAX10("sql_max", MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL),

    LEAST1("least", MonetDataType.INT, MonetDataType.INT, MonetDataType.INT),
    LEAST2("least", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    LEAST3("least", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    LEAST4("least", MonetDataType.DOUBLE, MonetDataType.DOUBLE, MonetDataType.DOUBLE),
    LEAST5("least", MonetDataType.DECIMAL, MonetDataType.DECIMAL, MonetDataType.DECIMAL),
    LEAST6("least", MonetDataType.TIME, MonetDataType.TIME, MonetDataType.TIME),
    LEAST7("least", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP),
    LEAST8("least", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.DATE),
    LEAST9("least", MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL),
    LEAST10("least", MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL),

    GREATEST1("greatest", MonetDataType.INT, MonetDataType.INT, MonetDataType.INT),
    GREATEST2("greatest", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    GREATEST3("greatest", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    GREATEST4("greatest", MonetDataType.DOUBLE, MonetDataType.DOUBLE, MonetDataType.DOUBLE),
    GREATEST5("greatest", MonetDataType.DECIMAL, MonetDataType.DECIMAL, MonetDataType.DECIMAL),
    GREATEST6("greatest", MonetDataType.TIME, MonetDataType.TIME, MonetDataType.TIME),
    GREATEST7("greatest", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP),
    GREATEST8("greatest", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.DATE),
    GREATEST9("greatest", MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL),
    GREATEST10("greatest", MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL),

    NULLIF1("nullif", MonetDataType.INT, MonetDataType.INT, MonetDataType.INT),
    NULLIF2("nullif", MonetDataType.REAL, MonetDataType.REAL, MonetDataType.REAL),
    NULLIF3("nullif", MonetDataType.STRING, MonetDataType.STRING, MonetDataType.STRING),
    NULLIF4("nullif", MonetDataType.DOUBLE, MonetDataType.DOUBLE, MonetDataType.DOUBLE),
    NULLIF5("nullif", MonetDataType.DECIMAL, MonetDataType.DECIMAL, MonetDataType.DECIMAL),
    NULLIF6("nullif", MonetDataType.TIME, MonetDataType.TIME, MonetDataType.TIME),
    NULLIF7("nullif", MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP, MonetDataType.TIMESTAMP),
    NULLIF8("nullif", MonetDataType.DATE, MonetDataType.DATE, MonetDataType.DATE),
    NULLIF9("nullif", MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL, MonetDataType.MONTH_INTERVAL),
    NULLIF10("nullif", MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL, MonetDataType.SECOND_INTERVAL),

    /*IFTHENELSE1("ifthenelse", MonetDataType.INT, MonetDataType.BOOLEAN, MonetDataType.INT, MonetDataType.INT),
    IFTHENELSE2("ifthenelse", MonetDataType.REAL, MonetDataType.BOOLEAN, MonetDataType.REAL, MonetDataType.REAL),
    IFTHENELSE3("ifthenelse", MonetDataType.STRING, MonetDataType.BOOLEAN, MonetDataType.STRING, MonetDataType.STRING),*/

    LOCAL_TIMEZONE("local_timezone", MonetDataType.SECOND_INTERVAL),

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
    TIME_HOUR("\"hour\"", MonetDataType.INT, MonetDataType.TIME),
    TIME_MINUTE("\"minute\"", MonetDataType.INT, MonetDataType.TIME),
    TIME_SECOND("\"second\"", MonetDataType.DECIMAL, MonetDataType.TIME),

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

    MONINT_YEAR("\"year\"", MonetDataType.INT, MonetDataType.MONTH_INTERVAL),
    MONINT_MONTH("\"month\"", MonetDataType.INT, MonetDataType.MONTH_INTERVAL),
    SECINT_DAY("\"day\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL),
    SECINT_HOUR("\"hour\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL),
    SECINT_MINUTE("\"minute\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL),
    SECINT_SECOND("\"second\"", MonetDataType.INT, MonetDataType.SECOND_INTERVAL);

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
