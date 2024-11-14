package co.mahmm.tokyo.commons.spec;

import lombok.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder

public class RunSpec {
    private String scenarioSpecFile;
    private String inputFile;
    private List<String> configFiles = new ArrayList<>();
    private ReportSpec reportSpec = new ReportSpec();
    private Map<String, Object> configs = new HashMap<>();

    public ReportSpec getReportSpec() {
        if(this.reportSpec == null) {
            return new ReportSpec();
        }
        return reportSpec;
    }
}
