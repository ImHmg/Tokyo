package co.mahmm.tokyo.commons.spec;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Builder
public class RunSpec {
    private String scenarioSpec;
    private List<String> configFiles = new ArrayList<>();
}
