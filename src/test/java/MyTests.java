import co.mahmm.tokyo.commons.spec.RunSpec;

import java.util.List;

public class MyTests extends TokyoRunner {
    static {
        TokyoRunner.addRunSpec( "scenarios/test.yaml", "env/local.yaml");
        TokyoRunner.addRunSpec( "scenarios/test2.yaml", "env/local.yaml");
    }
}
