package io.github.imhmg.tokyo.core.spec;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class StepSpec {

    private String id;
    private String name;
    private String type = "http";
    private String ref;
    private String configRef;
    private Map<String, String> configs = new HashMap<>();
    private String preHook;
    private String postHook;

}
