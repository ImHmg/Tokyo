package io.github.imhmg.tokyo.commons;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

public class YamlParser {
    private static ObjectMapper mapper;
    static {
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }
    public static <T> T parse(String text, Class<T> tClass) {
        try {
            return mapper.readValue(text, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public static <T> T parse(String text, TypeReference<T> tClass) {
        try {
            return mapper.readValue(text, tClass);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
