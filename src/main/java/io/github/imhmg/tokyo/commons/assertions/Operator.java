package io.github.imhmg.tokyo.commons.assertions;

import lombok.Getter;

@Getter
public enum Operator {
    EQ("[==]", "equals to"),
    NOT_EQ("[!=]", "not equals to"),
    CONTAINS("[<>]", "contains"),
    NOT_CONTAINS("[<!>]", "not contains");

    private final String description;
    private final String syntax;

    Operator(String syntax, String description) {
        this.syntax = syntax;
        this.description = description;
    }
    public static Operator getBySyntax(String syntax) {
        for (Operator op : values()) {
            if (op.getSyntax().equals(syntax)) {
                return op;
            }
        }
        throw new IllegalArgumentException("No Operator with syntax " + syntax + " found.");
    }
}