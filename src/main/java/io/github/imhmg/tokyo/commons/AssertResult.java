package io.github.imhmg.tokyo.commons;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AssertResult {
    private String name;
    private boolean status;
    private String expected;
    private String actual;

    public AssertResult(String name, boolean status) {
        this.name = name;
        this.status = status;
    }
}
