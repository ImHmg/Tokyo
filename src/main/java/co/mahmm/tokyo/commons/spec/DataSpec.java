package co.mahmm.tokyo.commons.spec;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class DataSpec {
    private String id;
    private String name;
    private String description;
    private Map<String, Object> data = new HashMap<>();
}
