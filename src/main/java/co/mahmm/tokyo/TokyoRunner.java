package co.mahmm.tokyo;

import co.mahmm.tokyo.commons.AssertResult;
import co.mahmm.tokyo.commons.spec.RunSpec;
import co.mahmm.tokyo.core.Step;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

public class TokyoRunner {

    List<SpecRunner> specs = new ArrayList<>();
    private static List<RunSpec> runSpecs = new ArrayList<>();

    @TestFactory
    Stream<DynamicContainer> runner() {
        return runSpecs.stream().map(run -> {
            SpecRunner specRunner = new SpecRunner(run.getScenarioSpec(), run.getConfigFiles());
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
        for (SpecRunner spec : this.specs) {
            System.out.println("Spec:" + spec.getSpec().getName());
            for (Map.Entry<String, List<Step>> e : spec.getScenario().getSteps().entrySet()) {
                System.out.println(e.getKey());
                for (Step step : e.getValue()) {
                    System.out.println("Name: " + step.getSpec().getName());
                    System.out.println("Passed: " + step.isPassed());
                    System.out.println("Asserts: ");
                    for (AssertResult assertsResult : step.getAssertsResults()) {
                        System.out.println("    " + assertsResult.getName() + " : " + assertsResult.isStatus());
                    }
                }
            }
        }
    }

}
