package co.mahmm.tokyo.http;

import co.mahmm.tokyo.commons.*;
import co.mahmm.tokyo.commons.spec.StepSpec;
import co.mahmm.tokyo.core.Context;
import co.mahmm.tokyo.core.Step;
import co.mahmm.tokyo.core.TokyoFaker;
import com.jayway.jsonpath.JsonPath;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import io.restassured.response.ResponseBodyExtractionOptions;
import io.restassured.specification.ProxySpecification;
import io.restassured.specification.RequestSpecification;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.Assertions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.diogonunes.jcolor.Ansi.*;
import static com.diogonunes.jcolor.Attribute.*;

@Getter
@Setter
public class HttpRequestStep extends Step {

    List<String> availableOperators = List.of("[==]", "[!=]", "[<>]", "[<!>]");
    private HttpSpec requestSpec;
    private ResponseBodyExtractionOptions responseBody;
    private Map<String, String> responseHeaders = new HashMap<>();
    private int responseStatusCode;
    private long testTime;
    private int fileParseRound = 0;
    private boolean isDone = false;
    private boolean assertStatus = false;

    private List<AssertResult> assertResults = new ArrayList<>();

    public HttpRequestStep(StepSpec requestSpec, Context context) {
        super(requestSpec, context);
        try {
            this.requestSpec = parseRefFileContent();
        } catch (Exception e) {
            throw new RuntimeException("Unable to parse step = " + getSpec().getId(), e);
        }
    }

    @Override
    public boolean process() {
        this.sendRequest();
        assertStatus = this.checkAsserts();
        if (!assertStatus) {
            isDone = true;
            return false;
        }
        this.captures();
        isDone = true;
        return true;
    }

    private HttpSpec parseRefFileContent() {
        Log.debug("Read http spec file = {}", this.getSpec().getRef());
        String refFileContent = FileReader.readFile(this.getSpec().getRef());
        Log.debug("Populate http spec file = {}", this.getSpec().getRef());
        refFileContent = populateVariables(refFileContent);
        Log.debug("Parse http spec file = {}", this.getSpec().getRef());
        return YamlParser.parse(refFileContent, HttpSpec.class);
    }

    private String populateVariables(String text) {
        if (fileParseRound > 15) {
            Log.debug("Populate recursive round limit exceed");
            return text;
        }
        List<String> variables = parseVariables(text);
        if (variables.size() == 0) {
            Log.debug("No variables found to populate");
            return text;
        }
        for (String key : variables) {
            String value = TokyoFaker.get(key);
            if (value == null) {
                value = getVar(key);
            }
            if (value != null) {
                text = text.replace("${" + key + "}", value);
            } else {
                Log.debug("Cannot find variable value for : {}", key);
            }
        }
        fileParseRound++;
        return populateVariables(text);
    }

    public List<String> parseVariables(String text) {
        List<String> variables = new ArrayList<>();
        int length = text.length();
        StringBuilder currentVariable = new StringBuilder();
        boolean insideVariable = false;
        int braceCount = 0; // To handle nested braces

        for (int i = 0; i < length; i++) {
            char currentChar = text.charAt(i);

            if (currentChar == '$' && i + 1 < length && text.charAt(i + 1) == '{') {
                // Starting a new variable
                insideVariable = true;
                braceCount = 1; // Reset brace count
                i++; // Skip past '{'
                currentVariable.setLength(0); // Clear the current variable
            } else if (insideVariable) {
                if (currentChar == '{') {
                    braceCount++; // Increment for nested brace
                } else if (currentChar == '}') {
                    braceCount--; // Decrement for closing brace
                    if (braceCount == 0) {
                        // Found a complete variable expression
                        variables.add(currentVariable.toString().trim());
                        insideVariable = false; // Reset for the next variable
                    }
                }
                if (braceCount > 0) {
                    currentVariable.append(currentChar); // Add character to current variable
                }
            }
        }

        return variables;
    }


    private void sendRequest() {
        RequestSpecification request = RestAssured.given();
        request = setRequestOptions(request);
        Log.debug("HTTP request, method: {}, url: {}", this.requestSpec.getMethod(), this.requestSpec.getEndpoint());
        Console.print(colorize(" Request ", BACK_COLOR(25, 217, 156), BLACK_TEXT(), BOLD()), colorize(" " + this.requestSpec.getMethod() + " ", BLACK_TEXT(), YELLOW_BACK(), BOLD()), " ", colorize(this.requestSpec.getEndpoint(), BLUE_TEXT()));
        Console.print("");
        if (this.requestSpec.getQueryParams() != null && !this.requestSpec.getQueryParams().isEmpty()) {
            Console.print(colorize("Request query", MAGENTA_TEXT(), BOLD()));
            for (Map.Entry<String, String> e : this.requestSpec.getQueryParams().entrySet()) {
                request = request.queryParam(e.getKey(), e.getValue());
                Log.debug("Request query parameter: {} : {}", e.getKey(), e.getValue());
                Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue()));
            }
            Console.print("");
        }
        if (this.requestSpec.getHeaders() != null && !this.requestSpec.getHeaders().isEmpty()) {
            Console.print(colorize("Request headers", MAGENTA_TEXT(), BOLD()));
            for (Map.Entry<String, String> e : this.requestSpec.getHeaders().entrySet()) {
                request = request.header(e.getKey(), e.getValue());
                Log.debug("Request header: {} : {}", e.getKey(), e.getValue());
                Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue()));
            }
            Console.print("");
        }
        if (this.requestSpec.getFormBody() != null && !this.requestSpec.getFormBody().isEmpty()) {
            Console.print(colorize("Request form body", MAGENTA_TEXT(), BOLD()));
            for (Map.Entry<String, Object> e : this.requestSpec.getFormBody().entrySet()) {
                request = request.formParam(e.getKey(), e.getValue());
                Log.debug("Form param: {} : {}", this.requestSpec.getRawBody());
                Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue().toString()));

            }
            Console.print("");
        }
        if (this.requestSpec.getRawBody() != null) {
            request.body(this.requestSpec.getRawBody());
            Console.print(colorize("Request body", MAGENTA_TEXT(), BOLD()));
            Console.print(colorize(this.requestSpec.getRawBody()));
            Log.debug("Request Body: {}", this.requestSpec.getRawBody());
            Console.print("");

        }

        request = request.when();
        long startTime = System.nanoTime();
        Response r = request.request(this.requestSpec.getMethod(), this.requestSpec.getEndpoint());
        long time = System.nanoTime() - startTime;
        this.testTime = (time / 1_000_000);
        ExtractableResponse<Response> res = r.then().extract();

        this.responseBody = res.body();
        this.responseStatusCode = res.statusCode();
        Console.print(colorize(" Response ", BACK_COLOR(74, 232, 93), BLACK_TEXT(), BOLD()), colorize(" " + this.responseStatusCode + " " + HTTPStatus.httpStatusMap.get(this.responseStatusCode) + " [" + (time / 1_000_000) + " ms] ", BACK_COLOR(85, 85, 85), TEXT_COLOR(255), BOLD()));
        Console.print("");

        Console.print(colorize("Response body", MAGENTA_TEXT(), BOLD()));
        Console.print(colorize(this.responseBody.asPrettyString()));
        Console.print("");

        Log.debug("Response timing: {}ms", time / 1_000_000);
        Log.debug("Response code: {}", res.statusCode());
        Log.debug("Response body: {}", res.body().asString());

        Console.print(colorize("Response headers", MAGENTA_TEXT(), BOLD()));
        for (Header header : res.headers()) {
            Log.debug("Response header: {} : {}", header.getName(), header.getValue());
            responseHeaders.put(header.getName().toLowerCase(), header.getValue());
            Console.print(colorize(header.getName() + " : ", BOLD()), colorize(header.getValue()));
        }
    }

    private RequestSpecification setRequestOptions(RequestSpecification request) {

        if (StringUtils.equalsIgnoreCase(getVar("TKY_DISABLE_SSL"), "TRUE")) {
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
        if (this.requestSpec.getCaptures() == null) {
            Log.debug("No captures found");
            return;
        }

        for (Map.Entry<String, String> e : this.requestSpec.getCaptures().entrySet()) {
            String value = extractResponseValues(e.getValue());
            Log.debug("Capture values, key: {}, value: {}", e.getKey(), value);
            setVar(e.getKey(), value);
        }
    }


    private boolean checkAsserts() {
        Log.debug("Start checking asserts");

        if (StringUtils.isNotBlank(this.requestSpec.getStatus())) {
            Assertions.assertEquals(String.valueOf(this.responseStatusCode), this.requestSpec.getStatus(), "Unexpected status code");
            assertResults.add(new AssertResult("Status code check", true));
        }

        if (this.requestSpec.getAsserts() == null) {
            Log.debug("No asserts found");
            return true;
        }

        for (Map.Entry<String, String> e : this.requestSpec.getAsserts().entrySet()) {
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
                actualValue = getValuesByExpression(this.responseBody.asString(), parseBodyAssertExpression.get("type"), parseBodyAssertExpression.get("expression"));
            }
            assertValues(actualValue, expectedValue, operator, assertKey);
        }
        return true;
    }

    private void assertValues(String actual, String expected, String operator, String key) {
        Log.debug("Assert values actual: {}, expected: {}, operator: {}, key: {}", actual, expected, operator, key);
        if (operator == null) {
            Assertions.assertNotNull(actual, key);
        }
        if ("[==]".equals(operator)) {
            Assertions.assertEquals(expected, actual, key);
        } else if ("[!=]".equals(operator)) {
            Assertions.assertNotEquals(expected, actual, key);
        } else if ("[<>]".equals(operator)) {
            Assertions.assertTrue(StringUtils.contains(actual, expected), key);
        } else if ("[<!>]".equals(operator)) {
            Assertions.assertFalse(StringUtils.contains(actual, expected), key);
        } else if ("[<...>]".equals(operator)) {
            // TODO Impletement between
            Assertions.assertTrue(StringUtils.contains(actual, expected), key);
        }
        assertResults.add(new AssertResult(key, true));
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
            value = getValuesByExpression(this.responseBody.asString(), parseBodyAssertExpression.get("type"), parseBodyAssertExpression.get("expression"));
        } else {
            throw new RuntimeException("Unable to parse expression " + expression);
        }
        return value;
    }


    private String getValuesByExpression(String response, String type, String expression) {
        if (type.equals("json")) {
            return JsonPath.parse(response).read(expression).toString();
        } else if (type.equals("xml")) {
//            return response.xmlPath().getString(expression);
            // TODO : implement xml path and regex
        } else if (type.equals("raw")) {
            return response;
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
        for (String availableOperator : availableOperators) {
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
            input = input.replaceFirst("raw", "");
        } else if (StringUtils.startsWith(input, "json:")) {
            result.put("type", "json");
            input = input.replaceFirst("json:", "");
        } else if (StringUtils.startsWith(input, "xml:")) {
            result.put("type", "xml");
            input = input.replaceFirst("xml:", "");
        }

        if (input.startsWith(" ")) {
            input = input.substring(1);
        }
        if (StringUtils.isEmpty(input)) {
            return result;
        }
        String[] split = null;
        for (String availableOperator : availableOperators) {
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
            return getValuesByExpression(this.responseBody.asString(), parseBodyAssertExpression.get("type"), parseBodyAssertExpression.get("expression"));
        } else if (StringUtils.startsWith(expression, "@queryParam")) {
            Map<String, String> parsedComps = parseKVAssertExpression("@queryParam", expression);
            String key = parsedComps.get("key");
            return this.requestSpec.getQueryParams().get(key);
        } else if (StringUtils.startsWith(expression, "@formParam")) {
            Map<String, String> parsedComps = parseKVAssertExpression("@formParam", expression);
            String key = parsedComps.get("key");
            return this.requestSpec.getQueryParams().get(key);
        } else if (StringUtils.startsWith(expression, "@url")) {
            return this.requestSpec.getEndpoint();
        } else if (StringUtils.startsWith(expression, "@method")) {
            return this.requestSpec.getMethod();
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
        return assertStatus;
    }

    @Override
    public long getTime() {
        return this.testTime;
    }

    @Override
    public List<AssertResult> getAssertsResults() {
        return this.assertResults;
    }

    @Override
    public String getAdditionalDetails() {
        StringBuilder request = new StringBuilder();
        request.append("<b>Method: </b><i>").append(this.requestSpec.getMethod()).append("</i><br>");
        request.append("<b>Endpoint: </b><i>").append(this.requestSpec.getEndpoint()).append("</i><br>");
        request.append("<b>Expected Status: </b><i>").append(this.requestSpec.getStatus()).append("</i><br>");

        if (this.requestSpec.getHeaders() != null && !this.requestSpec.getHeaders().isEmpty()) {
            request.append("<b>Request headers : </b><i>").append("<br>");
            for (Map.Entry<String, String> s : this.requestSpec.getHeaders().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue()).append("<br>");
            }
            request.append("</i>");
        }
        if (this.requestSpec.getQueryParams() != null && !this.requestSpec.getQueryParams().isEmpty()) {
            request.append("<b>Request query: </b><i>").append("<br>");
            for (Map.Entry<String, String> s : this.requestSpec.getQueryParams().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue()).append("<br>");
            }
            request.append("</i>");
        }
        if (this.requestSpec.getFormBody() != null && !this.requestSpec.getFormBody().isEmpty()) {
            request.append("<b>Request form params: </b><i>").append("<br>");
            for (Map.Entry<String, Object> s : this.requestSpec.getFormBody().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue().toString()).append("<br>");
            }
            request.append("</i>");
        }
        if (this.requestSpec.getRawBody() != null) {
            request.append("<b>Request body: </b><i>").append("<br>").append(this.requestSpec.getRawBody());
            request.append("</i>");
        }

        StringBuilder response = new StringBuilder();
        response.append("<b>Response status: </b><i>").append(this.responseStatusCode).append("<br>");
        if(!this.responseHeaders.isEmpty()) {
            response.append("<b>Response headers: </b><i>").append("<br>");
            for (Map.Entry<String, String> s : this.getResponseHeaders().entrySet()) {
                response.append("        ").append(s.getKey()).append(": ").append(s.getValue()).append("<br>");
            }
            response.append("</i>");
        }
        response.append("<b>Response body: </b><i>").append("<br>").append(this.responseBody.asPrettyString()).append("</i><br>");
        response.append("<b>Response time: </b><i>").append(this.testTime).append(" ms</i><br>");

        StringBuilder content = new StringBuilder();
        content.append("<pre><b><u>Request</u></b>").append("<br><br>").append(request.toString()).append("<br><br>");
        content.append("<b><u>Response</u></b>").append("<br><br>").append(response.toString()).append("<br><br></pre>");
        return content.toString();
    }

    @Override
    public boolean isDone() {
        return this.isDone;
    }
}
