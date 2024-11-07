package co.mahmm.tokyo.commons;

import co.mahmm.tokyo.SpecRunner;
import co.mahmm.tokyo.core.Step;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

public class ReportGenerator {

    public static void generateReports(List<SpecRunner> specs) {


        for (SpecRunner spec : specs) {

            Map<String, Object> report = new HashMap<>();
            report.put("title", StringUtils.defaultString(spec.getRunSpec().getReportTitle(), spec.getSpec().getName()));
            report.put("date", System.currentTimeMillis());
            report.put("user", StringUtils.defaultString(System.getenv("TKY_USER"), System.getProperty("user.name")));
            List<Object> sections = new ArrayList<>();
            report.put("sections", sections);


            for (Map.Entry<String, List<Step>> e : spec.getScenario().getSteps().entrySet()) {
                Map<String, Object> section = new HashMap<>();
                section.put("title", e.getKey());
                section.put("steps", new ArrayList<>());
                section.put("status", true);
                for (Step step : e.getValue()) {

                    Map<String, Object> s = new HashMap<>();
                    s.put("name", step.getSpec().getName());
                    s.put("status", step.isPassed());
                    if(!step.isPassed()) {
                        section.put("status", false);
                    }
                    s.put("asserts", new ArrayList<>());
                    for (AssertResult assertsResult : step.getAssertsResults()) {
                        ((ArrayList)s.get("asserts")).add(assertsResult);
                    }
                    s.put("details", step.getAdditionalDetails());
                    s.put("time", step.getTime());
                    ((ArrayList)section.get("steps")).add(s);
                }
                sections.add(section);
            }
            String fileName = spec.getSpec().getName();
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            String sanitizedFileName = fileName.replaceAll("[\\\\/:*?\"<>|]", "_");
            saveTestReport(JsonParser.toJson(report), sanitizedFileName + "_" + timeStamp);
        }
    }

    private static void saveTestReport(String content, String filename) {
        String s = FileReader.readFile("tky-test-report.html");
        String testdata = s.replace("__TESTDATA__", content);
        String buildDirPath = StringUtils.defaultString(System.getenv("TKY_REPORT_DIR"), "build/tokyo");
        File buildDir = new File(buildDirPath);

        if (!buildDir.exists()) {
            buildDir.mkdirs();
        }
        File file = new File(buildDir, filename + ".html");
        try {
            FileUtils.writeStringToFile(file, testdata);
            Log.debug("Report created at = {}", file.getAbsoluteFile());
        } catch (IOException e) {
            Log.error("Error while creating report", e.getMessage());
        }
    }

}
