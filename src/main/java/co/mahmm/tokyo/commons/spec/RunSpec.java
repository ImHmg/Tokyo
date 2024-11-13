package co.mahmm.tokyo.commons.spec;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder

public class RunSpec {
    private String scenarioSpecFile;
    private String inputFile;
    private List<String> configFiles = new ArrayList<>();
    private ReportSpec reportSpec = new ReportSpec();

    public ReportSpec getReportSpec() {
        if(this.reportSpec == null) {
            return new ReportSpec();
        }
        return reportSpec;
    }
}
