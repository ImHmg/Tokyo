import co.mahmm.tokyo.TokyoRunner;
import co.mahmm.tokyo.commons.spec.ReportSpec;
import co.mahmm.tokyo.commons.spec.RunSpec;
import org.junit.jupiter.api.AfterEach;

import java.util.List;


public class MyTests extends TokyoRunner {
    static {
        TokyoRunner.addRunSpec( "scenarios/test2.yaml", "env/local.yaml");
        TokyoRunner.addRunSpec(RunSpec.builder()
                        .scenarioSpec("scenarios/test.yaml")
                        .configFiles(List.of("env/local.yaml"))
                        .reportSpec(ReportSpec.builder()
                                .reportTitle("Custom title")
                                .user("Custom user name")
                                .completion(reportFilePath -> {
                                    System.out.println("report created at " + reportFilePath);
                                })
                                .build())
                .build());
    }
}
