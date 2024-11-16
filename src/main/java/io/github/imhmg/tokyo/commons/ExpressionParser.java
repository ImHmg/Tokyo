package io.github.imhmg.tokyo.commons;

import com.jayway.jsonpath.JsonPath;
import io.github.imhmg.tokyo.commons.assertions.Asserter;
import io.github.imhmg.tokyo.commons.assertions.Operator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class ExpressionParser {

    @Getter
    @Setter
    public static class Result {
        private String source;
        private String type;
        private String key;
        private Operator operator;
        private String expectedValue;
    }

    public static final String BODY = "@body";
    private static final String REPLACE_BODY = BODY + " ";
    public static final String HEADER = "@header";
    private static final String REPLACE_HEADER = HEADER + " ";
    public static final String STATUS = "@status";
    private static final String REPLACE_STATUS = STATUS + " ";

    public static final String JSON = "json";
    public static final String XML = "xml";
    public static final String RAW = "raw";

    private static final List<String> BODY_EXPRESSION_TYPES = List.of("json.", "xml.", "raw");

    public static Result parseExpression(String expression) {

        if (StringUtils.startsWith(expression, STATUS)) {
            return parseStatus(expression);
        }

        if (StringUtils.startsWith(expression, HEADER)) {
            return parseHeader(expression);
        }

        if (StringUtils.startsWith(expression, BODY)) {
            return parseBody(expression);
        }

        throw new IllegalArgumentException("Unable to parse expression = " + expression);
    }

    public static String extractValueByFormatAndExpression(String input, String format, String expression) {
        try {
            if (format.equals(JSON)) {
                return JsonPath.parse(input).read(expression).toString();
            } else if (format.equals(XML)) {
                throw new UnsupportedOperationException("xml expression not implemented yet");
            } else if (format.equals(RAW)) {
                return input;
            }
        } catch (Exception e) {
            Log.error("Error occurred while getting value for expression = {}", expression);
            return null;
        }
        throw new IllegalArgumentException("Unexpected format " + format);
    }

    private static Result parseStatus(String expression) {

        if (!expression.startsWith(REPLACE_STATUS) && !expression.equals(STATUS)) {
            throw new IllegalArgumentException("Unable to parse expression " + expression);
        }

        Result result = new Result();
        result.setSource(STATUS);
        expression = expression.replaceFirst(REPLACE_STATUS, "");
        String[] parts = expression.split(" ");
        if (parts.length == 2) {
            result.setOperator(Operator.getBySyntax(parts[0]));
            result.setExpectedValue(parts[1]);
        }
        return result;
    }

    private static Result parseHeader(String expression) {

        if (!expression.startsWith(REPLACE_HEADER)) {
            throw new IllegalArgumentException("Unable to parse expression " + expression);
        }
        expression = expression.replaceFirst(REPLACE_HEADER, "");
        Result result = parseKeyValueExpression(expression);
        result.setSource(HEADER);
        return result;
    }

    private static Result parseBody(String expression) {
        Result result = new Result();
        result.setSource(BODY);
        expression = expression.replaceFirst(REPLACE_BODY, "");
        String type = null;
        for (String et : BODY_EXPRESSION_TYPES) {
            if (expression.startsWith(et)) {
                type = et;
                break;
            }
        }
        if (type == null) {
            throw new IllegalArgumentException("Invalid @body expression. @body expression should be @body json.$.id [==] value");
        }
        result.setType(fixBodyType(type));
        expression = expression.replaceFirst(type, "");

        String[] split = null;
        for (Operator operator : Operator.values()) {
            if (expression.contains(" " + operator.getSyntax() + " ")) {
                split = StringUtils.splitByWholeSeparator(expression, " " + operator.getSyntax() + " ", 2);
                result.setOperator(operator);
            }
        }

        if (split == null) {
            result.setKey(StringUtils.isNoneEmpty(expression) ? expression : null);
        } else if (split.length == 2) {
            result.setExpectedValue(split[1]);
            result.setKey(split[0]);
        } else if (split.length == 1) {
            result.setExpectedValue(split[0]);
        }
        if(result.getType().equals(RAW) && result.getKey() != null) {
            throw new IllegalArgumentException("Invalid expression. Key not expected for type raw " + expression);
        }
        return result;
    }

    private static Result parseKeyValueExpression(String expression) {
        Result result = new Result();
        String[] split = null;
        for (Operator operator : Operator.values()) {
            if (expression.contains(" " + operator.getSyntax() + " ")) {
                split = StringUtils.splitByWholeSeparator(expression, " " + operator.getSyntax() + " ", 2);
                result.setOperator(operator);
            }
        }
        if (split == null) {
            result.setKey(expression);
        } else if (split.length == 2) {
            result.setKey(split[0]);
            result.setExpectedValue(split[1]);
        }
        return result;
    }

    private static String fixBodyType(String capturedType) {
        if(capturedType.startsWith(JSON)) {
            return JSON;
        }else if(capturedType.startsWith(XML)) {
            return XML;
        }else if(capturedType.startsWith(RAW)) {
            return RAW;
        }
        throw new IllegalArgumentException("Invalid body capture type");
    }

}
