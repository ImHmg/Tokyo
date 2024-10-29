package co.mahmm.tokyo.commons.spec;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class ScenarioSpec {
    private String name;
    private String description;
    private List<StepSpec> steps = new ArrayList<>();
    private List<DataSpec> inputs = new ArrayList<>();
    private Map<String, String> configs = new HashMap<>();
    private List<DataSpec> configsMap = new ArrayList<>();
}
