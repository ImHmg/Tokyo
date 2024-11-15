import io.github.imhmg.tokyo.TokyoRunner;
import io.github.imhmg.tokyo.core.spec.RunSpec;

import java.util.List;
import java.util.Map;


public class MyTests extends TokyoRunner {
    static {
        TokyoRunner.addRunSpec(RunSpec.builder()
                        .scenarioSpecFile("object/object-scenario.yaml")
                        .inputFile("object/object-inputs.csv")
                        .configFiles(List.of("object/env/local.yaml"))
                        .configs(Map.of("server", "localhost"))
                        .build());
    }
}
