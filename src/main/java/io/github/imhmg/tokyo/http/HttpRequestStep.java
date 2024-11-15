package io.github.imhmg.tokyo.http;

import io.github.imhmg.tokyo.commons.spec.StepSpec;
import io.github.imhmg.tokyo.core.Context;
import io.github.imhmg.tokyo.core.Step;
import com.jayway.jsonpath.JsonPath;
import io.github.imhmg.tokyo.commons.*;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.specification.ProxySpecification;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.function.Executable;
import org.opentest4j.AssertionFailedError;

import java.util.*;

import static com.diogonunes.jcolor.Ansi.*;
import static com.diogonunes.jcolor.Attribute.*;

@Getter
@Setter
public class HttpRequestStep extends Step {

    private static List<String> ASSERT_OPERATORS = List.of("[==]", "[!=]", "[<>]", "[<!>]");
    private static Map<String, String> ASSERT_OPERATOR_DESCRIPTIONS = Map.of(
            "[==]", "equals",
            "[!=]", "not equals",
            "[<>]", "contains",
            "[<!>]", "not contains"
    );
    private HttpSpec httpRequestSpec;
    private Optional<ResponseBodyExtractionOptions> responseBody = Optional.empty();
    private Map<String, String> responseHeaders = new HashMap<>();
    private int responseStatusCode;
    private long testExecutionTime;
    private boolean isExecutionSuccess = false;
    private boolean isHttpRequestSuccess = false;
    private boolean isAllAssertsSuccess = false;
    private Exception httpRequestException;
    private List<AssertResult> assertionsResults = new ArrayList<>();

    public HttpRequestStep(StepSpec requestSpec, Context context) {
        super(requestSpec, context);
        try {
            this.httpRequestSpec = parseRefFileContent();
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse step = " + getSpec().getName(), e);
        }
    }

    @Override
    public boolean process() {
        this.sendRequest();
        if (!isHttpRequestSuccess) {
            return false;
        }
        printCurl();
        List<Executable> executables = this.checkAsserts();
        Assertions.assertAll(this.getSpec().getName(), executables);
        isAllAssertsSuccess = true;
        this.captures();
        isExecutionSuccess = true;
        return true;
    }

    private HttpSpec parseRefFileContent() {
        // First pase and process defines
        Log.debug("Read http spec file = {}", this.getSpec().getRef());
        String refFileContent = FileReader.readFile(this.getSpec().getRef());
        Log.debug("Populate http spec file = {}", this.getSpec().getRef());
        refFileContent = VariableParser.replaceVariables(refFileContent, this::getVar);
        Log.debug("Parse http spec file = {}", this.getSpec().getRef());
        HttpSpec tempParse = YamlParser.parse(refFileContent, HttpSpec.class);
        parseDefines(tempParse);
        // Second parse
        Log.debug("Read http spec file = {}", this.getSpec().getRef());
        refFileContent = FileReader.readFile(this.getSpec().getRef());
        Log.debug("Populate http spec file = {}", this.getSpec().getRef());
        refFileContent = VariableParser.replaceVariables(refFileContent, this::getVar);
        Log.debug("Parse http spec file = {}", this.getSpec().getRef());
        return YamlParser.parse(refFileContent, HttpSpec.class);
    }

    private void sendRequest() {
        RequestSpecification request = RestAssured.given();
        request = setRequestOptions(request);
        Log.debug("HTTP request, method: {}, url: {}", this.httpRequestSpec.getMethod(), this.httpRequestSpec.getEndpoint());
        Console.print(colorize(" Request ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()), colorize(" " + this.httpRequestSpec.getMethod() + " ", BLACK_TEXT(), YELLOW_BACK(), BOLD()), " ", colorize(this.httpRequestSpec.getEndpoint(), BLUE_TEXT()));
        Console.print("");
        if (this.httpRequestSpec.getQueryParams() != null && !this.httpRequestSpec.getQueryParams().isEmpty()) {
            Console.print(colorize("Request query", MAGENTA_TEXT(), BOLD()));
            for (Map.Entry<String, String> e : this.httpRequestSpec.getQueryParams().entrySet()) {
                request = request.queryParam(e.getKey(), e.getValue());
                Log.debug("Request query parameter: {} : {}", e.getKey(), e.getValue());
                Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue()));
            }
            Console.print("");
        }
        if (this.httpRequestSpec.getHeaders() != null && !this.httpRequestSpec.getHeaders().isEmpty()) {
            Console.print(colorize("Request headers", MAGENTA_TEXT(), BOLD()));
            for (Map.Entry<String, String> e : this.httpRequestSpec.getHeaders().entrySet()) {
                request = request.header(e.getKey(), e.getValue());
                Log.debug("Request header: {} : {}", e.getKey(), e.getValue());
                Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue()));
            }
            Console.print("");
        }
        if (this.httpRequestSpec.getFormParams() != null && !this.httpRequestSpec.getFormParams().isEmpty()) {
            Console.print(colorize("Request form body", MAGENTA_TEXT(), BOLD()));
            for (Map.Entry<String, Object> e : this.httpRequestSpec.getFormParams().entrySet()) {
                request = request.formParam(e.getKey(), e.getValue());
                Log.debug("Form param: {} : {}", e.getKey(), e.getValue());
                Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue().toString()));

            }
            Console.print("");
        }
        if (this.httpRequestSpec.getBody() != null) {
            request.body(this.httpRequestSpec.getBody());
            Console.print(colorize("Request body", MAGENTA_TEXT(), BOLD()));
            Console.print(colorize(this.httpRequestSpec.getBody()));
            Log.debug("Request Body : {}", this.httpRequestSpec.getBody());
            Console.print("");
        }

        request = request.when();
        long startTime = System.nanoTime();
        try {
            Response r = request.request(this.httpRequestSpec.getMethod(), this.httpRequestSpec.getEndpoint());
            this.testExecutionTime = ((System.nanoTime() - startTime) / 1_000_000);
            ExtractableResponse<Response> res = r.then().extract();
            this.responseBody = Optional.of(res.body());
            this.responseStatusCode = res.statusCode();

            for (Header header : res.headers()) {
                Log.debug("Response header: {} : {}", header.getName(), header.getValue());
                responseHeaders.put(header.getName().toLowerCase(), header.getValue());
            }
            Log.debug("Response timing: {}ms", this.testExecutionTime);
            Log.debug("Response code: {}", res.statusCode());
            Log.debug("Response body: {}", res.body().asString());
            Console.print(colorize(" Response ", BACK_COLOR(25, 217, 156), BLACK_TEXT(), BOLD()), colorize(" " + this.responseStatusCode + " " + HTTPStatus.httpStatusMap.get(this.responseStatusCode) + " [" + this.testExecutionTime + " ms] ", BACK_COLOR(85, 85, 85), TEXT_COLOR(255), BOLD()));
            Console.print("");
            Console.print(colorize("Response body", MAGENTA_TEXT(), BOLD()));
            Console.print(colorize(this.responseBody.get().asPrettyString()));
            Console.print("");
            Console.print(colorize("Response headers", MAGENTA_TEXT(), BOLD()));
            for (Header header : res.headers()) {
                Log.debug("Response header: {} : {}", header.getName(), header.getValue());
                responseHeaders.put(header.getName().toLowerCase(), header.getValue());
                Console.print(colorize(header.getName() + " : ", BOLD()), colorize(header.getValue()));
            }
            isHttpRequestSuccess = true;
        } catch (Exception e) {
            this.httpRequestException = e;
            this.testExecutionTime = ((System.nanoTime() - startTime) / 1_000_000);
            Console.print(colorize(" ERROR OCCURRED ", BACK_COLOR(243, 80, 127), BLACK_TEXT(), BOLD()));
            Console.print(colorize(e.getMessage(), BOLD()));
            printCurl();
            System.out.println("\n\n");
            e.printStackTrace();
            this.assertionsResults.add(new AssertResult("Exception occurred : " + e.getMessage(), false));
            Assertions.assertTrue(false, "No exception occurred");
        }
    }

    private RequestSpecification setRequestOptions(RequestSpecification request) {
        if (StringUtils.equalsIgnoreCase(getVar("TKY_DISABLE_SSL"), "TRUE") ||
            StringUtils.equalsIgnoreCase(String.valueOf(this.httpRequestSpec.getOptions().get("TKY_DISABLE_SSL")), "TRUE")) {
            Log.debug("SSL verification disabled");
            request = request.relaxedHTTPSValidation();
        }
        if (StringUtils.isNoneBlank(getVar("TKY_PROXY_HOST"), getVar("TKY_PROXY_PORT"))) {
            ProxySpecification proxySpecification = ProxySpecification.host(getVar("TKY_PROXY_HOST"))
                    .withPort(Integer.parseInt(getVar("TKY_PROXY_PORT")));

            if (StringUtils.isNotBlank(getVar("TKY_PROXY_USERNAME"))) {
                proxySpecification = proxySpecification.withAuth(getVar("TKY_PROXY_USERNAME"), getVar("TKY_PROXY_PASSWORD"));
            }
            request = request.proxy(proxySpecification);
        }
        return request;
    }

    private void captures() {
        if (this.httpRequestSpec.getCaptures() == null) {
            Log.debug("No captures found");
            return;
        }
        Console.print("\n");
        Console.print(colorize(" Captures ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()));

        for (Map.Entry<String, String> e : this.httpRequestSpec.getCaptures().entrySet()) {
            String value = extractResponseValues(e.getValue());
            Log.debug("Capture values, key: {}, value: {}", e.getKey(), value);
            Console.print(e.getKey()," = ", value);
            setVar(e.getKey(), value);
        }
    }

    private List<Executable> checkAsserts() {
        Log.debug("Start checking asserts");
        Console.print(colorize(" Assertions ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()));
        List<Executable> assertions = new ArrayList<>();
        if (StringUtils.isNotBlank(this.httpRequestSpec.getStatus())) {
            assertions.add(assertValues(String.valueOf(this.responseStatusCode), this.httpRequestSpec.getStatus(), "[==]", "Status code check"));
        }

        if (this.httpRequestSpec.getAsserts() == null) {
            Log.debug("No asserts found");
            return assertions;
        }

        for (Map.Entry<String, String> e : this.httpRequestSpec.getAsserts().entrySet()) {
            Log.debug("Start checking assert = {}, expression = {}", e.getKey(), e.getValue());
            String assertKey = e.getKey();
            String assertExpression = e.getValue();
            String actualValue = null;
            String operator = null;
            String expectedValue = null;
            if (StringUtils.startsWith(assertExpression, "@status")) {
                actualValue = String.valueOf(this.responseStatusCode);
                String exp = assertExpression.replaceFirst("@status ", "");
                String[] parts = exp.split(" ");
                if (parts.length == 2) {
                    operator = parts[0];
                    expectedValue = parts[1];
                }
            } else if (StringUtils.startsWith(assertExpression, "@header")) {
                Map<String, String> parseHeaderAssertExpression = parseKVAssertExpression("@header", assertExpression);
                String headerKey = parseHeaderAssertExpression.get("key");
                actualValue = this.responseHeaders.get(headerKey.toLowerCase());
                operator = parseHeaderAssertExpression.get("operator");
                expectedValue = parseHeaderAssertExpression.get("value");
            } else if (StringUtils.startsWith(assertExpression, "@body")) {
                Map<String, String> parseBodyAssertExpression = parseBodyAssertExpression(assertExpression);
                operator = parseBodyAssertExpression.get("operator");
                expectedValue = parseBodyAssertExpression.get("value");
                if (this.responseBody.isPresent()) {
                    actualValue = getValuesByExpression(this.responseBody.get().asString(), parseBodyAssertExpression.get("type"), parseBodyAssertExpression.get("expression"));
                }
            }
            assertions.add(assertValues(actualValue, expectedValue, operator, assertKey));
        }
        return assertions;
    }

    private Executable assertValues(String actual, String expected, String operator, String key) {
        return () -> {
            Log.debug("Assert values actual: {}, expected: {}, operator: {}, key: {}", actual, expected, operator, key);
            try {
                if (operator == null) {
                    Assertions.assertNotNull(actual, key);
                }else if ("[==]".equals(operator)) {
                    Assertions.assertEquals(expected, actual, key);
                } else if ("[!=]".equals(operator)) {
                    Assertions.assertNotEquals(expected, actual, key);
                } else if ("[<>]".equals(operator)) {
                    Assertions.assertTrue(StringUtils.contains(actual, expected), key);
                } else if ("[<!>]".equals(operator)) {
                    Assertions.assertFalse(StringUtils.contains(actual, expected), key);
                } else if ("[<...>]".equals(operator)) {
                    // TODO Impletement between
                    throw new UnsupportedOperationException("range compare implemented yet");
//                    Assertions.assertTrue(StringUtils.contains(actual, expected), key);
                }else{
                    throw new UnsupportedOperationException("unsupported operator " + operator);
                }
                Console.print(colorize("Pass : " + key, TEXT_COLOR(66, 247, 112), BOLD()));
                if(operator == null) {
                    Console.print("    ", colorize("Actual: ", BOLD()),  actual);
                    Console.print("    ", colorize("Expected: ", BOLD()),  "<not null>");
                }else{
                    Console.print("    ", colorize("Actual: ", BOLD()),  actual);
                    Console.print("    ", colorize("Operator: ", BOLD()),  operator);
                    Console.print("    ", colorize("Expected: ", BOLD()),  expected);
                }

                assertionsResults.add(new AssertResult(key, true, expected, actual));
            } catch (AssertionFailedError ex) {
                String e = ex.getExpected() == null ? "" : ex.getExpected().getStringRepresentation();
                String a = ex.getActual() == null ? "" : ex.getActual().getStringRepresentation();
                Console.print(colorize("Fail : " + key, TEXT_COLOR(247, 66, 66), BOLD()));

                if(operator == null) {
                    Console.print("    ", colorize("Actual: ", BOLD()),  actual);
                    Console.print("    ", colorize("Expected: ", BOLD()),  "<not null>");
                }else{
                    Console.print("    ", colorize("Actual: ", BOLD()),  actual);
                    Console.print("    ", colorize("Operator: ", BOLD()),  operator);
                    Console.print("    ", colorize("Expected: ", BOLD()),  expected);
                }
                assertionsResults.add(new AssertResult(key, false, e, a));
                throw ex;
            }
        };
    }

    public String extractResponseValues(String expression) {
        String value = null;
        if (StringUtils.startsWith(expression, "@status")) {
            value = String.valueOf(this.responseStatusCode);
        } else if (StringUtils.startsWith(expression, "@header")) {
            Map<String, String> parseHeaderAssertExpression = parseKVAssertExpression("@header", expression);
            String headerKey = parseHeaderAssertExpression.get("key");
            value = this.responseHeaders.get(headerKey.toLowerCase());
        } else if (StringUtils.startsWith(expression, "@body")) {
            Map<String, String> parseBodyAssertExpression = parseBodyAssertExpression(expression);
            if (this.responseBody.isPresent()) {
                value = getValuesByExpression(this.responseBody.get().asString(), parseBodyAssertExpression.get("type"), parseBodyAssertExpression.get("expression"));
            }
        } else {
            throw new RuntimeException("Unable to parse expression " + expression);
        }
        return value;
    }

    private String getValuesByExpression(String response, String type, String expression) {
        try {
            if (type.equals("json")) {
                return JsonPath.parse(response).read(expression).toString();
            } else if (type.equals("xml")) {
//            return response.xmlPath().getString(expression);
                // TODO : implement xml path and regex
                throw new UnsupportedOperationException("xml path not implemented yet");
            } else if (type.equals("raw")) {
                return response;
            }
        } catch (Exception e) {
            Log.error("Error occurred while getting value for expression = {}", expression);
            return null;
        }
        throw new RuntimeException("Unexpected type in assert");
    }

    private Map<String, String> parseKVAssertExpression(String type, String expression) {
        Map<String, String> result = new HashMap<>();
        result.put("key", null);
        result.put("operator", null);
        result.put("value", null);

        expression = expression.replaceFirst(type + " ", "");

        String[] split = null;
        for (String availableOperator : ASSERT_OPERATORS) {
            if (expression.contains(" " + availableOperator + " ")) {
                split = StringUtils.splitByWholeSeparator(expression, " " + availableOperator + " ", 2);
                result.put("operator", availableOperator);
            }
        }
        if (split == null) {
            result.put("key", expression);
        } else if (split.length == 2) {
            result.put("value", split[1]);
            result.put("key", split[0]);
        }
        return result;
    }

    /*
        Parse body value extract expression to map
        eg : @body json $.id [==] 1234
        type = json
        expression = $.id
        operator = [==]
        value = 1234
     */
    private Map<String, String> parseBodyAssertExpression(String input) {
        String originalInput = input;
        Map<String, String> result = new HashMap<>();
        result.put("type", null);
        result.put("expression", null);
        result.put("operator", null);
        result.put("value", null);

        input = input.replaceFirst("@body ", "");
        if (StringUtils.startsWith(input, "raw")) {
            result.put("type", "raw");
            input = input.replaceFirst("raw ", "");
        } else if (StringUtils.startsWith(input, "json ")) {
            result.put("type", "json");
            input = input.replaceFirst("json ", "");
        } else if (StringUtils.startsWith(input, "xml ")) {
            result.put("type", "xml");
            input = input.replaceFirst("xml ", "");
        }

        if (input.startsWith(" ")) {
            input = input.substring(1);
        }
        if (StringUtils.isEmpty(input)) {
            return result;
        }
        String[] split = null;
        for (String availableOperator : ASSERT_OPERATORS) {
            if (input.contains(" " + availableOperator + " ")) {
                split = StringUtils.splitByWholeSeparator(input, " " + availableOperator + " ", 2);
                result.put("operator", availableOperator);
            }
        }
        if (split == null) {
            result.put("expression", input);
        } else if (split.length == 2) {
            result.put("value", split[1]);
            result.put("expression", split[0]);
        } else if (split.length == 1) {
            result.put("value", split[0]);
        }
        return result;
    }

    public String extractRequestValues(String expression) {
        if (StringUtils.startsWith(expression, "@header")) {
            Map<String, String> parseHeaderAssertExpression = parseKVAssertExpression("@header", expression);
            String headerKey = parseHeaderAssertExpression.get("key");
            return this.responseHeaders.get(headerKey.toLowerCase());
        } else if (StringUtils.startsWith(expression, "@body")) {
            Map<String, String> parseBodyAssertExpression = parseBodyAssertExpression(expression);
            if (this.responseBody.isEmpty()) {
                return null;
            }
            return getValuesByExpression(this.responseBody.get().asString(), parseBodyAssertExpression.get("type"), parseBodyAssertExpression.get("expression"));
        } else if (StringUtils.startsWith(expression, "@queryParam")) {
            Map<String, String> parsedComps = parseKVAssertExpression("@queryParam", expression);
            String key = parsedComps.get("key");
            return this.httpRequestSpec.getQueryParams().get(key);
        } else if (StringUtils.startsWith(expression, "@formParam")) {
            Map<String, String> parsedComps = parseKVAssertExpression("@formParam", expression);
            String key = parsedComps.get("key");
            return this.httpRequestSpec.getQueryParams().get(key);
        } else if (StringUtils.startsWith(expression, "@url")) {
            return this.httpRequestSpec.getEndpoint();
        } else if (StringUtils.startsWith(expression, "@method")) {
            return this.httpRequestSpec.getMethod();
        }
        throw new RuntimeException("Unable to parse expression " + expression);
    }

    @Override
    public String getStepVariables(String key) {
        if (key.startsWith(this.getSpec().getId() + ".request ")) {
            key = key.replaceFirst(this.getSpec().getId() + ".request ", "");
            return extractRequestValues(key);
        }
        if (key.startsWith(this.getSpec().getId() + ".response ")) {
            key = key.replaceFirst(this.getSpec().getId() + ".response ", "");
            return extractResponseValues(key);
        }
        return null;
    }

    @Override
    public boolean isPassed() {
        return isAllAssertsSuccess;
    }

    @Override
    public long getTime() {
        return this.testExecutionTime;
    }

    @Override
    public List<AssertResult> getAssertsResults() {
        return this.assertionsResults;
    }

    @Override
    public String getAdditionalDetails() {
        StringBuilder request = new StringBuilder();
        request.append("<b>Method: </b><i>").append(this.httpRequestSpec.getMethod()).append("</i><br>");
        request.append("<b>Endpoint: </b><i>").append(this.httpRequestSpec.getEndpoint()).append("</i><br>");
        request.append("<b>Expected Status: </b><i>").append(this.httpRequestSpec.getStatus()).append("</i><br>");

        if (this.httpRequestSpec.getHeaders() != null && !this.httpRequestSpec.getHeaders().isEmpty()) {
            request.append("<b>Request headers : </b><i>").append("<br>");
            for (Map.Entry<String, String> s : this.httpRequestSpec.getHeaders().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue()).append("<br>");
            }
            request.append("</i>");
        }
        if (this.httpRequestSpec.getQueryParams() != null && !this.httpRequestSpec.getQueryParams().isEmpty()) {
            request.append("<b>Request query: </b><i>").append("<br>");
            for (Map.Entry<String, String> s : this.httpRequestSpec.getQueryParams().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue()).append("<br>");
            }
            request.append("</i>");
        }
        if (this.httpRequestSpec.getFormParams() != null && !this.httpRequestSpec.getFormParams().isEmpty()) {
            request.append("<b>Request form params: </b><i>").append("<br>");
            for (Map.Entry<String, Object> s : this.httpRequestSpec.getFormParams().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue().toString()).append("<br>");
            }
            request.append("</i>");
        }
        if (this.httpRequestSpec.getBody() != null) {
            request.append("<b>Request body: </b><i>").append("<br>").append(this.httpRequestSpec.getBody());
            request.append("</i>");
        }

        StringBuilder response = new StringBuilder();
        if (this.responseBody.isPresent()) {
            response.append("<b>Response status: </b><i>").append(this.responseStatusCode).append("<br>");
            if (!this.responseHeaders.isEmpty()) {
                response.append("<b>Response headers: </b><i>").append("<br>");
                for (Map.Entry<String, String> s : this.getResponseHeaders().entrySet()) {
                    response.append("        ").append(s.getKey()).append(": ").append(s.getValue()).append("<br>");
                }
                response.append("</i>");
            }
            String body = null;
            if (this.responseBody.isPresent()) {
                body = this.responseBody.get().asPrettyString();
            }
            response.append("<b>Response body: </b><i>").append("<br>").append(StringEscapeUtils.escapeHtml4(body)).append("</i><br>");
            response.append("<b>Response time: </b><i>").append(this.testExecutionTime).append(" ms</i><br>");
        } else if (this.httpRequestException != null) {
            response.append("<b>Exception: </b><i>").append("").append(StringEscapeUtils.escapeHtml4(this.httpRequestException.getLocalizedMessage())).append("</i><br>");
        }

        StringBuilder content = new StringBuilder();
        content.append("<pre><b><u>Request</u></b>").append("<br><br>").append(request.toString()).append("<br><br>");
        content.append("<b><u>Response</u></b>").append("<br><br>").append(response.toString()).append("<br><br>");
        content.append("</pre>");
        return content.toString();
    }

    public boolean isExecutionSuccess() {
        return this.isExecutionSuccess;
    }

    private void printCurl() {
        System.out.println("\n");
        Console.print(colorize(" CURL ",  BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()));
        Console.print(colorize(generateCurlCommand()));
        System.out.println("\n");
    }

    private String generateCurlCommand() {
        StringBuilder curlCommand = new StringBuilder("curl -X ");
        curlCommand.append(this.httpRequestSpec.getMethod()).append(" ");
        curlCommand.append("'").append(this.httpRequestSpec.getEndpoint()).append("' ");

        if (this.httpRequestSpec.getHeaders() != null && !this.httpRequestSpec.getHeaders().isEmpty()) {
            curlCommand.append(" \\\n");
            for (Map.Entry<String, String> e : this.httpRequestSpec.getHeaders().entrySet()) {
                curlCommand.append("-H '").append(e.getKey()).append(": ")
                        .append(e.getValue()).append("' ");
            }
        }

        if (this.httpRequestSpec.getQueryParams() != null && !this.httpRequestSpec.getQueryParams().isEmpty()) {
            curlCommand.append(" \\\n");
            for (Map.Entry<String, String> e : this.httpRequestSpec.getQueryParams().entrySet()) {
                curlCommand.append("--data-urlencode '").append(e.getKey()).append("=")
                        .append(e.getValue()).append("' ");
            }
        }

//        if (this.requestSpec.getFormBody() != null && !this.requestSpec.getFormBody().isEmpty()) {
//            Console.print(colorize("Request form body", MAGENTA_TEXT(), BOLD()));
//            for (Map.Entry<String, Object> e : this.requestSpec.getFormBody().entrySet()) {
//                request = request.formParam(e.getKey(), e.getValue());
//                Log.debug("Form param: {} : {}", this.requestSpec.getRawBody());
//                Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue().toString()));
//
//            }
//            Console.print("");
//        }

        if (this.httpRequestSpec.getBody() != null) {
            curlCommand.append(" \\\n");
            curlCommand.append("-d '").append(this.httpRequestSpec.getBody()).append("' ");
        }
        return curlCommand.toString();
    }

    private void parseDefines(HttpSpec spec) {
        Log.debug("Parse defines");
        if(spec == null) {
            return;
        }
        for (Map.Entry<String, Object> entry : spec.getDefine().entrySet()) {
            Log.debug("Parse defines add vars {} : {}", entry.getKey(), entry.getValue());
            setVar(entry.getKey(), String.valueOf(entry.getValue()));
        }
    }


}
