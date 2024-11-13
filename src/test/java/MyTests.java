import co.mahmm.tokyo.TokyoRunner;
import co.mahmm.tokyo.commons.spec.RunSpec;

import java.util.List;


public class MyTests extends TokyoRunner {
    static {
        TokyoRunner.addRunSpec(RunSpec.builder()
                        .scenarioSpecFile("scenarios/test.yaml")
                        .inputFile("file://C:\\Users\\Hasitha\\OneDrive\\Desktop\\Work\\HttpEngine\\src\\test\\resources\\sample.csv")
                        .configFiles(List.of("env/local.yaml"))
                        .build());
    }
}
