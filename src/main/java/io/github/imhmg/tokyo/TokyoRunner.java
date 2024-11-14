package io.github.imhmg.tokyo;

import io.github.imhmg.tokyo.commons.ReportGenerator;
import io.github.imhmg.tokyo.commons.spec.RunSpec;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DynamicContainer;
import org.junit.jupiter.api.TestFactory;

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

    public static void addRunSpec(RunSpec spec) {
        runSpecs.add(spec);
    }

    @AfterEach
    public  void generateReport() {
        ReportGenerator.generateReports(this.specs);
    }



}
