import co.mahmm.tokyo.TokyoRunner;


public class MyTests extends TokyoRunner {
    static {
        TokyoRunner.addRunSpec( "scenarios/test.yaml", "env/local.yaml");
        TokyoRunner.addRunSpec( "scenarios/test2.yaml", "env/local.yaml");
    }
}
