import co.mahmm.tokyo.SpecRunner;
import co.mahmm.tokyo.commons.spec.RunSpec;
import co.mahmm.tokyo.core.Context;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

public class TokyoRunner {

    private static List<RunSpec> runSpecs = new ArrayList<>();

    @TestFactory
    Stream<DynamicContainer> runner() {
        return runSpecs.stream().parallel().map(run -> {
            SpecRunner specRunner = new SpecRunner(run.getScenarioSpec(), run.getConfigFiles());
            String envDescription = "";
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

}
