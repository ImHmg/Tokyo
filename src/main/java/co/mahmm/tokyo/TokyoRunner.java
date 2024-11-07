package co.mahmm.tokyo;

import co.mahmm.tokyo.commons.AssertResult;
import co.mahmm.tokyo.commons.FileReader;
import co.mahmm.tokyo.commons.JsonParser;
import co.mahmm.tokyo.commons.ReportGenerator;
import co.mahmm.tokyo.commons.spec.RunSpec;
import co.mahmm.tokyo.core.Step;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

public class TokyoRunner {

    List<SpecRunner> specs = new ArrayList<>();
    private static List<RunSpec> runSpecs = new ArrayList<>();

    @TestFactory
    Stream<DynamicContainer> runner() {
        return runSpecs.stream().map(run -> {
            SpecRunner specRunner = new SpecRunner(run);
            String envDescription = "";
            specs.add(specRunner);
            return DynamicContainer.dynamicContainer("Spec : " + specRunner.getSpec().getName() + envDescription, specRunner.run());
        });
    }

    public static void addRunSpec(String scenarioFile) {
        runSpecs.add(RunSpec.builder().scenarioSpec(scenarioFile).build());
    }

    public static void addRunSpec(String scenarioFile, List<String> configFiles) {
        runSpecs.add(RunSpec.builder().scenarioSpec(scenarioFile).configFiles(configFiles).build());
    }

    public static void addRunSpec(String scenarioFile, String... configFiles) {
        runSpecs.add(RunSpec.builder().scenarioSpec(scenarioFile).configFiles(Arrays.asList(configFiles)).build());
    }

    @AfterEach
    public  void generateReport() {
        ReportGenerator.generateReports(this.specs);
    }



}
