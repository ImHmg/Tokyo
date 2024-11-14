package io.github.imhmg.tokyo.commons;

import io.github.imhmg.tokyo.SpecRunner;
import io.github.imhmg.tokyo.core.Step;
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

            String title = spec.getRunSpec().getReportSpec().getReportTitle();
            if(StringUtils.isEmpty(title)) {
                title = spec.getSpec().getName();
            }

            String user = spec.getRunSpec().getReportSpec().getUser();
            if(StringUtils.isEmpty(user)) {
                user = System.getProperty("user.name");
            }

            report.put("title", title);
            report.put("date",  new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format(new Date()));
            report.put("user", user);
            List<Object> sections = new ArrayList<>();
            report.put("sections", sections);


            int total = 0;
            int passed = 0;
            int failed = 0;

            for (Map.Entry<String, List<Step>> e : spec.getScenario().getSteps().entrySet()) {
                Map<String, Object> section = new HashMap<>();
                section.put("title", e.getKey());
                section.put("steps", new ArrayList<>());
                section.put("status", true);
                for (Step step : e.getValue()) {

                    Map<String, Object> s = new HashMap<>();
                    s.put("name", step.getSpec().getName());
                    s.put("status", step.isPassed());
                    total++;
                    if(!step.isPassed()) {
                        section.put("status", false);
                        failed++;
                    }else{
                        passed++;
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
            report.put("totalCount", total);
            report.put("passedCount", passed);
            report.put("failedCount", failed);

            String filename = spec.getRunSpec().getReportSpec().getFile();
            if(filename == null) {
                filename = StringUtils.defaultString(System.getenv("TKY_REPORT_DIR"), "build/tokyo");
                String r = spec.getSpec().getName();
                r = r.replaceAll("[\\\\/:*?\"<>|]", "_");
                r = r + "_" +new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".html";
                filename += "/" + r;
            }
            saveTestReport(JsonParser.toJson(report), filename);
            if(spec.getRunSpec().getReportSpec().getCompletion() != null) {
                Log.debug("Calling completion block");
                spec.getRunSpec().getReportSpec().getCompletion().completion(filename);
            }
        }
    }

    private static void saveTestReport(String content, String filePath) {
        String s = FileReader.readFile("tky-test-report.html");
        String testdata = s.replace("__TESTDATA__", content);
        File file = new File(filePath);
        if(!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        try {
            FileUtils.writeStringToFile(file, testdata);
            Log.debug("Report created at = {}", file.getAbsoluteFile());
        } catch (IOException e) {
            Log.error("Error while creating report", e.getMessage());
        }
    }

}
