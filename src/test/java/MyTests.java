import co.mahmm.tokyo.TokyoRunner;
import co.mahmm.tokyo.commons.spec.RunSpec;

import java.util.List;


public class MyTests extends TokyoRunner {
    static {
        TokyoRunner.addRunSpec(RunSpec.builder()
                        .scenarioSpecFile("object/object-scenario.yaml")
                        .inputFile("object/object-inputs.csv")
                        .configFiles(List.of("object/env/local.yaml"))
                        .build());
    }
}
