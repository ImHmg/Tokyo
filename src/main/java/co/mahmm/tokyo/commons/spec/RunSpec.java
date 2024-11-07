package co.mahmm.tokyo.commons.spec;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class RunSpec {
    private String scenarioSpec;
    private String reportTitle;
    private List<String> configFiles = new ArrayList<>();

}
