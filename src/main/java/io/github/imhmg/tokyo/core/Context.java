package io.github.imhmg.tokyo.core;

import io.github.imhmg.tokyo.core.spec.DataSpec;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Context {
    private Map<String, Object> vars = new HashMap<>();
    private List<Step> steps = new ArrayList<>();
    private Map<String, Object> configs = new HashMap<>();
    private DataSpec inputs = new DataSpec();
    public Map<String, Object> getInputData() {
        return inputs.getData();
    }

    public String getStepVariable(String key) {
        if(!key.startsWith("step.")) {
            return null;
        }
        key = key.replaceFirst("step.", "");
        String[] split = key.split("\\.");

        for (Step step : steps) {
            if(step.isExecutionSuccess() && step.getSpec().getId().equals(split[0])) {
                return step.getStepVariables(key);
            }
        }
        return null;
    }

}
