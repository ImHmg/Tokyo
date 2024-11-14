package io.github.imhmg.tokyo.http;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public class HttpSpec {
    @JsonProperty("Name")
    private String name;
    @JsonProperty("Method")
    private String method;
    @JsonProperty("Endpoint")
    private String endpoint;
    @JsonProperty("TextBody")
    private String textBody;
    @JsonProperty("JsonBody")
    private String jsonBody;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("FormParams")
    private Map<String, Object> formParams;
    @JsonProperty("Define")
    private Map<String, Object> define = new HashMap<>();
    @JsonProperty("MultipartData")
    private Map<String, Object> multipartData = new HashMap<>(); // TODO : Implement
    @JsonProperty("Options")
    private Map<String, Object> options = new HashMap<>();
    @JsonProperty("Headers")
    private Map<String, String> headers = new HashMap<>();
    @JsonProperty("QueryParams")
    private Map<String, String> queryParams = new HashMap<>();
    @JsonProperty("Captures")
    private Map<String, String> captures;
    @JsonProperty("Asserts")
    private Map<String, String> asserts;

    public String getBody() {
        return StringUtils.defaultString(this.jsonBody, this.textBody);
    }

    public Map<String, String> getHeaders() {
        if(this.jsonBody != null && !this.headers.containsKey("Content-Type")) {
            this.headers.put("Content-Type", "application/json");
        }
        return headers;
    }
}
