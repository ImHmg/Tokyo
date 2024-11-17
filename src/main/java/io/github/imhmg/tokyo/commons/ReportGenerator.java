package io.github.imhmg.tokyo.commons;

import io.github.imhmg.tokyo.SpecRunner;
import io.github.imhmg.tokyo.commons.assertions.AssertResult;
import io.github.imhmg.tokyo.core.Step;
import io.github.imhmg.tokyo.core.http.HttpRequestStep;
import io.github.imhmg.tokyo.core.http.HttpResponse;
import io.github.imhmg.tokyo.core.http.HttpSpec;
import io.restassured.http.Header;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportGenerator {

    public static void generateReports(List<SpecRunner> specs) {


        for (SpecRunner spec : specs) {
            Object report = generateReportForSpecRunner(spec);
            String filename = spec.getRunSpec().getReportSpec().getFile();
            if (filename == null) {
                filename = StringUtils.defaultString(System.getenv("TKY_REPORT_DIR"), "build/tokyo");
                String r = spec.getSpec().getName();
                r = r.replaceAll("[\\\\/:*?\"<>|]", "_");
                r = r + "_" + new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".html";
                filename += "/" + r;
            }
            saveTestReport(JsonParser.toJson(report), filename);
            if (spec.getRunSpec().getReportSpec().getCompletion() != null) {
                Log.debug("Calling completion block");
                spec.getRunSpec().getReportSpec().getCompletion().completion(filename);
            }
        }
    }

    private static Map<String, Object> generateReportForSpecRunner(SpecRunner spec) {
        Map<String, Object> report = new HashMap<>();

        // Set title
        String title = spec.getRunSpec().getReportSpec().getReportTitle();
        if (StringUtils.isEmpty(title)) {
            title = spec.getSpec().getName();
        }

        // Set user
        String user = spec.getRunSpec().getReportSpec().getUser();
        if (StringUtils.isEmpty(user)) {
            user = System.getProperty("user.name");
        }

        report.put("title", title);
        report.put("user", user);
        report.put("date", new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
        List<Map<String, Object>> sections = getSectionsReport(spec);

        int totalSteps = 0;
        int passedSteps = 0;
        int failedSteps = 0;
        int totalSections = sections.size();
        int passedSections = 0;
        int failedSections = 0;

        for (Map<String, Object> o : sections) {
            if (o.get("status").equals(true)) {
                passedSections++;
            } else {
                failedSections++;
            }
            totalSteps += NumberUtils.toInt(o.get("totalSteps").toString(), 0);
            passedSteps += NumberUtils.toInt(o.get("passedSteps").toString(), 0);
            failedSteps += NumberUtils.toInt(o.get("failedSteps").toString(), 0);
        }
        report.put("totalSteps", totalSteps);
        report.put("passedSteps", passedSteps);
        report.put("failedSteps", failedSteps);
        report.put("totalSections", totalSections);
        report.put("passedSections", passedSections);
        report.put("failedSections", failedSections);

        report.put("sections", sections);
        return report;
    }

    private static List<Map<String, Object>> getSectionsReport(SpecRunner spec) {
        List<Map<String, Object>> sections = new ArrayList<>();
        for (Map.Entry<String, List<Step>> e : spec.getScenario().getSteps().entrySet()) {
            Map<String, Object> section = new HashMap<>();
            section.put("title", e.getKey());
            section.put("steps", new ArrayList<>());
            int totalSteps = e.getValue().size();
            int passedSteps = 0;
            int failedSteps = 0;
            List<Object> steps = new ArrayList<>();
            for (Step step : e.getValue()) {
                Map<String, Object> s = new HashMap<>();
                s.put("name", step.getSpec().getName());
                s.put("status", step.isPassed());
                if (step.isPassed()) {
                    passedSteps++;
                } else {
                    failedSteps++;
                }
                s.put("asserts", step.getAssertsResults());
                s.put("details", step.getAdditionalDetails());
                s.put("time", step.getTime());
                steps.add(s);
            }
            section.put("steps", steps);
            section.put("totalSteps", totalSteps);
            section.put("passedSteps", passedSteps);
            section.put("failedSteps", failedSteps);
            section.put("status", failedSteps == 0);
            sections.add(section);
        }
        return sections;
    }


    private static void saveTestReport(String content, String filePath) {
        String s = FileReader.readFile("tky-test-report.html");
        String testdata = s.replace("__TESTDATA__", content);
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            FileUtils.writeStringToFile(file, testdata);
            Log.debug("Report created at = {}", file.getAbsoluteFile());
        } catch (IOException e) {
            Log.error("Error while creating report", e.getMessage());
        }
    }

    public static String generateHTMLForStep(HttpRequestStep step) {
        HttpSpec httpRequestSpec = step.getHttpRequestSpec();
        boolean isHttpRequestFinished = step.isHttpRequestFinished();
        HttpResponse httpResponse = step.getHttpResponse();

        StringBuilder request = new StringBuilder();
        request.append("<b>Method: </b><i>").append(httpRequestSpec.getMethod()).append("</i><br>");
        request.append("<b>Endpoint: </b><i>").append(httpRequestSpec.getEndpoint()).append("</i><br>");
        request.append("<b>Expected Status: </b><i>").append(httpRequestSpec.getStatus()).append("</i><br>");

        if (httpRequestSpec.getHeaders() != null && !httpRequestSpec.getHeaders().isEmpty()) {
            request.append("<b>Request headers : </b><i>").append("<br>");
            for (Map.Entry<String, String> s : httpRequestSpec.getHeaders().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue()).append("<br>");
            }
            request.append("</i>");
        }
        if (httpRequestSpec.getQueryParams() != null && !httpRequestSpec.getQueryParams().isEmpty()) {
            request.append("<b>Request query: </b><i>").append("<br>");
            for (Map.Entry<String, String> s : httpRequestSpec.getQueryParams().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue()).append("<br>");
            }
            request.append("</i>");
        }
        if (httpRequestSpec.getFormParams() != null && !httpRequestSpec.getFormParams().isEmpty()) {
            request.append("<b>Request form params: </b><i>").append("<br>");
            for (Map.Entry<String, Object> s : httpRequestSpec.getFormParams().entrySet()) {
                request.append("        ").append(s.getKey()).append(": ").append(s.getValue().toString()).append("<br>");
            }
            request.append("</i>");
        }
        if (httpRequestSpec.getBody() != null) {
            request.append("<b>Request body: </b><i>").append("<br>").append(httpRequestSpec.getBody());
            request.append("</i>");
        }

        StringBuilder response = new StringBuilder();
        if (isHttpRequestFinished) {
            response.append("<b>Response status: </b><i>").append(httpResponse.getStatus()).append("<br>");
            if (!httpResponse.getHeaders().exist()) {
                response.append("<b>Response headers: </b><i>").append("<br>");
                for (Header s : httpResponse.getHeaders().asList()) {
                    response.append("        ").append(s.getName()).append(": ").append(s.getValue()).append("<br>");
                }
                response.append("</i>");
            }
            String body = httpResponse.getPrettyBody();
            response.append("<b>Response body: </b><i>").append("<br>").append(StringEscapeUtils.escapeHtml4(body)).append("</i><br>");
            response.append("<b>Response time: </b><i>").append(httpResponse.getTime()).append(" ms</i><br>");
        } else if (step.getHttpRequestException() != null) {
            response.append("<b>Exception: </b><i>").append("").append(StringEscapeUtils.escapeHtml4(step.getHttpRequestException().getLocalizedMessage())).append("</i><br>");
        }

        StringBuilder content = new StringBuilder();
        content.append("<pre><b><u>Request</u></b>").append("<br><br>").append(request.toString()).append("<br><br>");
        content.append("<b><u>Response</u></b>").append("<br><br>").append(response.toString()).append("<br><br>");
        content.append("</pre>");
        return content.toString();
    }

}
