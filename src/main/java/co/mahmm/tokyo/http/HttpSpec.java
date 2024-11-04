package co.mahmm.tokyo.http;

import lombok.Getter;
import lombok.Setter;

import java.util.Map;

@Getter
@Setter
public class HttpSpec {
    private String name;
    private String method;
    private String endpoint;
    private String rawBody;
    private String status;
    private Map<String, Object> formBody;
    private Map<String, String> headers;
    private Map<String, String> queryParams;
    private Map<String, String> captures;
    private Map<String, String> asserts;


}
