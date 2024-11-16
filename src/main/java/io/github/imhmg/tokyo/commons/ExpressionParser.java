package io.github.imhmg.tokyo.commons;

import com.jayway.jsonpath.JsonPath;
import io.github.imhmg.tokyo.commons.assertions.Operator;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

public class ExpressionParser {

    @Getter
    @Setter
    public static class Result {
        private Source source;
        private Format format;
        private String key;
        private Operator operator;
        private String expectedValue;
    }

    @Getter
    public enum Source {
        BODY("@body", "@body "),
        HEADER("@header", "@header "),
        STATUS("@status", "@status ");

        private final String replace;
        private final String syntax;

        Source(String syntax, String replace) {
            this.syntax = syntax;
            this.replace = replace;
        }
        public static Source getBySyntax(String syntax) {
            for (Source op : values()) {
                if (op.getSyntax().equals(syntax)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("No Source with syntax " + syntax + " found.");
        }
    }

    @Getter
    public enum Format {
        JSON("json", "json."),
        XML("xml", "xml."),
        RAW("raw", "raw");

        private final String syntax;
        private final String format;

        Format(String format, String syntax) {
            this.format = format;
            this.syntax = syntax;
        }
        public static Format getByFormat(String format) {
            for (Format op : values()) {
                if (op.getFormat().equals(format)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("No Format with format " + format + " found.");
        }

        public static Format getBySyntax(String syntax) {
            for (Format op : values()) {
                if (op.getSyntax().equals(syntax)) {
                    return op;
                }
            }
            throw new IllegalArgumentException("No Format with syntax " + syntax + " found.");
        }
    }

    public static Result parseExpression(String expression) {

        if (StringUtils.startsWith(expression, Source.STATUS.syntax)) {
            return parseStatus(expression);
        }

        if (StringUtils.startsWith(expression, Source.HEADER.syntax)) {
            return parseHeader(expression);
        }

        if (StringUtils.startsWith(expression, Source.BODY.syntax)) {
            return parseBody(expression);
        }

        throw new IllegalArgumentException("Unable to parse expression = " + expression);
    }

    public static String extractValueByFormatAndExpression(String input, Format format, String expression) {
        try {
            if (format == Format.JSON) {
                return JsonPath.parse(input).read(expression).toString();
            } else if (format == Format.XML) {
                throw new UnsupportedOperationException("xml expression not implemented yet");
            } else if (format == Format.RAW) {
                return input;
            }
        } catch (Exception e) {
            Log.error("Error occurred while getting value for expression = {}", expression);
            return null;
        }
        throw new IllegalArgumentException("Unexpected format " + format);
    }

    private static Result parseStatus(String expression) {

        if (!expression.startsWith(Source.STATUS.replace) && !expression.equals(Source.STATUS.syntax)) {
            throw new IllegalArgumentException("Unable to parse expression " + expression);
        }

        Result result = new Result();
        result.setSource(Source.STATUS);
        expression = expression.replaceFirst(Source.STATUS.replace, "");
        String[] parts = expression.split(" ");
        if (parts.length == 2) {
            result.setOperator(Operator.getBySyntax(parts[0]));
            result.setExpectedValue(parts[1]);
        }
        return result;
    }

    private static Result parseHeader(String expression) {

        if (!expression.startsWith(Source.HEADER.replace)) {
            throw new IllegalArgumentException("Unable to parse expression " + expression);
        }
        expression = expression.replaceFirst(Source.HEADER.replace, "");
        Result result = parseKeyValueExpression(expression);
        result.setSource(Source.HEADER);
        return result;
    }

    private static Result parseBody(String expression) {
        Result result = new Result();
        result.setSource(Source.BODY);
        expression = expression.replaceFirst(Source.BODY.replace, "");
        Format format = null;
        for (Format et : Format.values()) {
            if (expression.startsWith(et.syntax)) {
                format = et;
                break;
            }
        }
        if (format == null) {
            throw new IllegalArgumentException("Invalid @body expression. @body expression should be @body json.$.id [==] value");
        }
        result.setFormat(format);
        expression = expression.replaceFirst(format.syntax, "");

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
        if(result.getFormat() == Format.RAW && result.getKey() != null) {
            throw new IllegalArgumentException("Invalid expression. Key not expected for format raw " + expression);
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

}
