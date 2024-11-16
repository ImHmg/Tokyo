package io.github.imhmg.tokyo.core.http;

import io.github.imhmg.tokyo.commons.assertions.AssertResult;
import io.github.imhmg.tokyo.commons.assertions.Operator;
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
        // First parse and process defines
        Log.debug("Read http spec file = {}", this.getSpec().getRef());
        String refFileContent = FileReader.readFile(this.getSpec().getRef());
        refFileContent = VariableParser.replaceVariables(refFileContent, this::getVar);
        HttpSpec tempParse = YamlParser.parse(refFileContent, HttpSpec.class);
        parseDefines(tempParse);
        // Second parse
        refFileContent = FileReader.readFile(this.getSpec().getRef());
        refFileContent = VariableParser.replaceVariables(refFileContent, this::getVar);
        return YamlParser.parse(refFileContent, HttpSpec.class);
    }

    private void parseDefines(HttpSpec spec) {
        Log.debug("Parse defines");
        if (spec == null) {
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

        Console.print(
                colorize(" Request ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()),
                colorize(" " + this.httpRequestSpec.getMethod() + " ", BLACK_TEXT(), YELLOW_BACK(), BOLD()), " ",
                colorize(this.httpRequestSpec.getEndpoint(), BLUE_TEXT()),
                "\n"
        );

        request = setQueryParameters(request);
        request = setHeaders(request);
        request = setFormData(request);
        request = setBody(request);
        request = request.when();
        try {
            Response response = request.request(this.httpRequestSpec.getMethod(), this.httpRequestSpec.getEndpoint());
            this.httpResponse = new HttpResponse(response.then().extract());
            isHttpRequestFinished = true;
        }catch (Exception exception) {
            httpRequestException = exception;

            Console.print(
                    colorize(" ERROR OCCURRED ", BACK_COLOR(243, 80, 127), BLACK_TEXT(), BOLD()),
                    "\n",
                    colorize(exception.getMessage(), BOLD()),
                    "\n\n"
            );

            exception.printStackTrace();
            this.assertionsResults.add(new AssertResult("Exception occurred : " + exception.getMessage(), false));
            Assertions.fail("Exception occurred");
        }

        Console.print(
                colorize(" Response ", BACK_COLOR(25, 217, 156), BLACK_TEXT(), BOLD()),
                colorize(" " + this.httpResponse.getStatus() + " " + HTTPStatus.httpStatusMap.get(this.httpResponse.getStatus()) + " [" + this.httpResponse.getTime() + " ms] ", BACK_COLOR(85, 85, 85), TEXT_COLOR(255), BOLD()),
                "\n\n",
                colorize("Response body", MAGENTA_TEXT(), BOLD()),
                "\n",
                colorize(this.httpResponse.getPrettyBody()),
                "\n\n",
                colorize("Response headers", MAGENTA_TEXT(), BOLD())
        );

        for (Header header : this.httpResponse.getHeaders()) {
            Console.print(colorize(header.getName() + " : ", BOLD()), colorize(header.getValue()));
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

        Console.print("\n",colorize(" Captures ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()));

        for (Map.Entry<String, String> e : this.httpRequestSpec.getCaptures().entrySet()) {
            String value = getResponseValuesByExpression(ExpressionParser.parseExpression(e.getValue()));
            Log.debug("Capture values, key: {}, value: {}", e.getKey(), value);
            Console.print(e.getKey(), " = ", value);
            setVar(e.getKey(), value);
        }
    }

    private List<Executable> processAsserts() {
        Log.debug("Start checking asserts");
        Console.print("\n", colorize(" Assertions ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()));
        List<Executable> assertions = new ArrayList<>();
        // Check http status code
        if (StringUtils.isNotBlank(this.httpRequestSpec.getStatus())) {
            assertions.add(assertValues(String.valueOf(this.httpResponse.getStatus()), this.httpRequestSpec.getStatus(), Operator.EQ, "Status code check"));
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

    private Executable assertValues(String actual, String expected, Operator operator, String key) {
        return () -> {
            Log.debug("Assert values actual: {}, expected: {}, operator: {}, key: {}", actual, expected, operator, key);
            try {
                if (operator == null) {
                    Assertions.assertNotNull(actual, key);
                } else if ("[==]".equals(operator.getSyntax())) {
                    Assertions.assertEquals(expected, actual, key);
                } else if ("[!=]".equals(operator.getSyntax())) {
                    Assertions.assertNotEquals(expected, actual, key);
                } else if ("[<>]".equals(operator.getSyntax())) {
                    Assertions.assertTrue(StringUtils.contains(actual, expected), key);
                } else if ("[<!>]".equals(operator.getSyntax())) {
                    Assertions.assertFalse(StringUtils.contains(actual, expected), key);
                } else if ("[<...>]".equals(operator.getSyntax())) {
                    throw new UnsupportedOperationException("range compare implemented yet");
                } else {
                    throw new UnsupportedOperationException("unsupported operator " + operator.getSyntax());
                }
                Console.print(colorize("Pass : " + key, TEXT_COLOR(66, 247, 112), BOLD()));
                if (operator == null) {
                    Console.print("     ", colorize("Actual: ", BOLD()), actual);
                    Console.print("     ", colorize("Expected: ", BOLD()), "<not null>");
                } else {
                    Console.print("     ", colorize("Actual: ", BOLD()), actual);
                    Console.print("     ", colorize("Operator: ", BOLD()), operator.getSyntax());
                    Console.print("     ", colorize("Expected: ", BOLD()), expected);
                }

                assertionsResults.add(new AssertResult(key, true, expected, actual));
            } catch (AssertionFailedError ex) {
                String e = ex.getExpected() == null ? "" : ex.getExpected().getStringRepresentation();
                String a = ex.getActual() == null ? "" : ex.getActual().getStringRepresentation();
                Console.print(colorize("Fail : " + key, TEXT_COLOR(247, 66, 66), BOLD()));

                if (operator == null) {
                    Console.print("     ", colorize("Actual: ", BOLD()), actual);
                    Console.print("     ", colorize("Expected: ", BOLD()), "<not null>");
                } else {
                    Console.print("     ", colorize("Actual: ", BOLD()), actual);
                    Console.print("     ", colorize("Operator: ", BOLD()), operator.getSyntax());
                    Console.print("     ", colorize("Expected: ", BOLD()), expected);
                }
                assertionsResults.add(new AssertResult(key, false, e, a));
                throw ex;
            }
        };
    }

    public String getResponseValuesByExpression(ExpressionParser.Result parseExpression) {
        String value = null;
        if (parseExpression.getSource().equals(ExpressionParser.STATUS)) {
            value = String.valueOf(this.httpResponse.getStatus());
        } else if (parseExpression.getSource().equals(ExpressionParser.HEADER)) {
            value = this.httpResponse.getHeaders().getValue(parseExpression.getKey());
        } else if (parseExpression.getSource().equals(ExpressionParser.BODY) && this.httpResponse.getBody() != null) {
            value = ExpressionParser.extractValueByFormatAndExpression(this.httpResponse.getBody(), parseExpression.getType(), parseExpression.getKey());
        } else {
            throw new IllegalArgumentException("Invalid source " + parseExpression.getSource());
        }
        return value;
    }

    private RequestSpecification setQueryParameters(RequestSpecification request) {
        if (this.httpRequestSpec.getQueryParams() == null || this.httpRequestSpec.getQueryParams().isEmpty()) {
            return request;
        }
        Console.print(colorize("Request query", MAGENTA_TEXT(), BOLD()));
        for (Map.Entry<String, String> e : this.httpRequestSpec.getQueryParams().entrySet()) {
            request = request.queryParam(e.getKey(), e.getValue());
            Log.debug("Request query parameter: {} : {}", e.getKey(), e.getValue());
            Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue()));
        }
        Console.print("");
        return request;
    }

    private RequestSpecification setHeaders(RequestSpecification request) {
        if (this.httpRequestSpec.getHeaders() == null || this.httpRequestSpec.getHeaders().isEmpty()) {
            return request;
        }
        Console.print(colorize("Request headers", MAGENTA_TEXT(), BOLD()));
        for (Map.Entry<String, String> e : this.httpRequestSpec.getHeaders().entrySet()) {
            request = request.header(e.getKey(), e.getValue());
            Log.debug("Request header: {} : {}", e.getKey(), e.getValue());
            Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue()));
        }
        Console.print("");
        return request;
    }

    private RequestSpecification setFormData(RequestSpecification request) {
        if (this.httpRequestSpec.getFormParams() == null || this.httpRequestSpec.getFormParams().isEmpty()) {
            return request;
        }
        Console.print(colorize("Request form body", MAGENTA_TEXT(), BOLD()));
        for (Map.Entry<String, Object> e : this.httpRequestSpec.getFormParams().entrySet()) {
            request = request.formParam(e.getKey(), e.getValue());
            Log.debug("Form param: {} : {}", e.getKey(), e.getValue());
            Console.print(colorize(e.getKey() + " : ", BOLD()), colorize(e.getValue().toString()));

        }
        Console.print("");
        return request;
    }

    private RequestSpecification setBody(RequestSpecification request) {
        if (this.httpRequestSpec.getBody() == null) {
            return request;
        }
        request = request.body(this.httpRequestSpec.getBody());
        Console.print(colorize("Request body", MAGENTA_TEXT(), BOLD()));
        Console.print(colorize(this.httpRequestSpec.getBody()));
        Log.debug("Request Body : {}", this.httpRequestSpec.getBody());
        Console.print("");
        return request;
    }

    private void logRequest() {
        Console.print(colorize(" Request ", BACK_COLOR(90, 124, 255), BLACK_TEXT(), BOLD()), colorize(" " + this.httpRequestSpec.getMethod() + " ", BLACK_TEXT(), YELLOW_BACK(), BOLD()), " ", colorize(this.httpRequestSpec.getEndpoint(), BLUE_TEXT()));
        Console.print("");
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
        return ReportGenerator.generateHTMLForStep(this);
    }

    public boolean isExecutionSuccess() {
        return this.isExecutionSuccess;
    }


}
