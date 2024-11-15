package io.github.imhmg.tokyo.core.http;

import io.restassured.http.Headers;
import io.restassured.response.ExtractableResponse;
import io.restassured.response.Response;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class HttpResponse {

    private String body;
    private String prettyBody;
    private Headers headers = new Headers();
    private int status;
    private long time;

    public HttpResponse(ExtractableResponse<Response> response) {
        this.body = response.asString();
        this.time = response.time();
        this.prettyBody = response.asPrettyString();
        this.headers = response.headers();
    }


}
