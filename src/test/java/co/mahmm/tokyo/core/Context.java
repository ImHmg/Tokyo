package co.mahmm.tokyo.core;

import co.mahmm.tokyo.commons.spec.DataSpec;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class Context {
    private Map<String, String> configs = new HashMap<>();
    private DataSpec inputs = new DataSpec();
    private Map<String, String> vars = new HashMap<>();
    private List<Step> steps = new ArrayList<>();
    public Map<String, String> getInputData() {
        return inputs.getData();
    }

    public String getStepVariable(String key) {
        if(!key.startsWith("step.")) {
            return null;
        }
        key = key.replaceFirst("step.", "");
        String[] split = key.split("\\.");
        for (Step step : steps) {
            if(step.isDone() && step.getSpec().getId().equals(split[0])) {
                return step.getStepVariables(key);
            }
        }
        return null;
    }

}
