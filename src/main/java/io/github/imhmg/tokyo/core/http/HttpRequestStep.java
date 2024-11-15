package io.github.imhmg.tokyo.core.http;

import io.github.imhmg.tokyo.core.spec.StepSpec;
import io.github.imhmg.tokyo.core.Context;
import io.github.imhmg.tokyo.core.Step;
import io.github.imhmg.tokyo.commons.*;
import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.response.Response;
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

    private HttpResponse httpResponse;
    private boolean isExecutionSuccess = false;
    private boolean isHttpRequestFinished = false;
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
        if (!isHttpRequestFinished) {
            return false;
        }
        List<Executable> executables = this.processAsserts();
        Assertions.assertAll(this.getSpec().getName(), executables);
        isAllAssertsSuccess = true;
        this.processCaptures();
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
        try {
            Response r = request.request(this.httpRequestSpec.getMethod(), this.httpRequestSpec.getEndpoint());
            this.httpResponse = new HttpResponse(r.then().extract());
            isHttpRequestFinished = true;

            Log.debug("Response timing: {}ms", this.httpResponse.getTime());
            Log.debug("Response code: {}", this.httpResponse.getStatus());
            Log.debug("Response body: {}", this.httpResponse.getBody());

            Console.print(colorize(" Response ", BACK_COLOR(25, 217, 156), BLACK_TEXT(), BOLD()), colorize(" " + this.httpResponse.getStatus() + " " + HTTPStatus.httpStatusMap.get(this.httpResponse.getStatus()) + " [" + this.httpResponse.getTime() + " ms] ", BACK_COLOR(85, 85, 85), TEXT_COLOR(255), BOLD()));
            Console.print("");
            Console.print(colorize("Response body", MAGENTA_TEXT(), BOLD()));
            Console.print(colorize(this.httpResponse.getPrettyBody()));
            Console.print("");
            Console.print(colorize("Response headers", MAGENTA_TEXT(), BOLD()));
            for (Header header : this.httpResponse.getHeaders()) {
                Console.print(colorize(header.getName() + " : ", BOLD()), colorize(header.getValue()));
            }
        } catch (Exception e) {
            this.httpRequestException = e;
            Console.print(colorize(" ERROR OCCURRED ", BACK_COLOR(243, 80, 127), BLACK_TEXT(), BOLD()));
            Console.print(colorize(e.getMessage(), BOLD()));
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

    private void processCaptures() {
        if (this.httpRequestSpec.getCaptures() == null) {
            Log.debug("No captures found");
            return;
        }
        Console.print("\n");
        Console.print(colorize(" Captures ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()));

        for (Map.Entry<String, String> e : this.httpRequestSpec.getCaptures().entrySet()) {
            String value = getResponseValuesByExpression(ExpressionParser.parseExpression(e.getValue()));
            Log.debug("Capture values, key: {}, value: {}", e.getKey(), value);
            Console.print(e.getKey()," = ", value);
            setVar(e.getKey(), value);
        }
    }

    private List<Executable> processAsserts() {
        Log.debug("Start checking asserts");
        Console.print(colorize(" Assertions ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()));

        List<Executable> assertions = new ArrayList<>();

        // Check http status code
        if (StringUtils.isNotBlank(this.httpRequestSpec.getStatus())) {
            assertions.add(assertValues(String.valueOf(this.httpResponse.getStatus()), this.httpRequestSpec.getStatus(), "[==]", "Status code check"));
        }

        // No asserts found
        if (this.httpRequestSpec.getAsserts() == null) {
            Log.debug("No asserts found");
            return assertions;
        }

        for (Map.Entry<String, String> e : this.httpRequestSpec.getAsserts().entrySet()) {
            Log.debug("Start checking assert = {}, expression = {}", e.getKey(), e.getValue());
            String assertMessage = e.getKey();
            ExpressionParser.Result parseExpression = ExpressionParser.parseExpression(e.getValue());
            String actualValue = getResponseValuesByExpression(parseExpression);
            assertions.add(assertValues(actualValue, parseExpression.getExpectedValue(), parseExpression.getOperator(), assertMessage));
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

    public String getResponseValuesByExpression(ExpressionParser.Result parseExpression) {
        String value = null;
        if(parseExpression.getSource().equals(ExpressionParser.STATUS)) {
            value = String.valueOf(this.httpResponse.getStatus());
        } else if (parseExpression.getSource().equals(ExpressionParser.HEADER)) {
            value = this.httpResponse.getHeaders().getValue(parseExpression.getKey());
        } else if (parseExpression.getSource().equals(ExpressionParser.BODY) && this.httpResponse.getBody() != null) {
            value = ExpressionParser.extractValueByFormatAndExpression(this.httpResponse.getBody(), parseExpression.getType(), parseExpression.getKey());
        }else{
            throw new IllegalArgumentException("Invalid source " + parseExpression.getSource());
        }
        return value;
    }


    @Override
    public String getStepVariables(String key) {
        // TODO implement step variables
        return null;
    }

    @Override
    public boolean isPassed() {
        return isAllAssertsSuccess;
    }

    @Override
    public long getTime() {
        return this.httpResponse.getTime();
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
        if (isHttpRequestFinished) {
            response.append("<b>Response status: </b><i>").append(this.httpResponse.getStatus()).append("<br>");
            if (!this.httpResponse.getHeaders().exist()) {
                response.append("<b>Response headers: </b><i>").append("<br>");
                for (Header s : this.httpResponse.getHeaders().asList()) {
                    response.append("        ").append(s.getName()).append(": ").append(s.getValue()).append("<br>");
                }
                response.append("</i>");
            }
            String body = this.httpResponse.getPrettyBody();
            response.append("<b>Response body: </b><i>").append("<br>").append(StringEscapeUtils.escapeHtml4(body)).append("</i><br>");
            response.append("<b>Response time: </b><i>").append(this.httpResponse.getTime()).append(" ms</i><br>");
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


}
