import io.github.imhmg.tokyo.TokyoRunner;
import io.github.imhmg.tokyo.core.spec.RunSpec;
import io.github.imhmg.tokyo.util.ProductsMockAPI;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.AfterAll;

import java.io.IOException;
import java.util.List;
import java.util.Map;


public class ProductFlowTest extends TokyoRunner {
    private static MockWebServer mockWebServer;

    static {
         mockWebServer = new MockWebServer();
        try {
            mockWebServer.start(52001);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        mockWebServer.setDispatcher(new ProductsMockAPI());

        RunSpec spec = RunSpec.builder()
                .configFiles(List.of("product/env.yaml"))
                .inputFile("product/product-input.csv")
                .scenarioSpecFile("product/flow-1.yaml")
                .configs(Map.of("port", "" + 52001, "domain", mockWebServer.getHostName()))
                .build();

        TokyoRunner.addRunSpec(spec);
    }

    @AfterAll
    public static void afterAll() throws IOException {
        mockWebServer.shutdown();
    }
}
