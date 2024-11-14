package io.github.imhmg.tokyo.commons.spec;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DataSpec {
    private String name;
    private Map<String, Object> data = new HashMap<>();
}
