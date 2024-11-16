package io.github.imhmg.tokyo.commons;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class VariableParserTest {

    Map<String, String> vars = Map.of(
            "firstName", "John",
            "lastName", "Doe",
            "city", "New York",
            "country", "USA",
            "greeting", "Hello",
            "nested1", "${firstName}",
            "nested2", "${nested1}",
            "nested3", "${nested2}",
            "spaced key", "value with spaces",
            "complexKey", "JohnDoe"
    );

    Map<String, String> templates = Map.of(
            "Welcome, ${firstName} ${lastName}!", "Welcome, John Doe!",
            "Location: ${city}, ${country}", "Location: New York, USA",
            "${greeting}, ${firstName}!", "Hello, John!",
            "Deeply nested: ${nested3}", "Deeply nested: John",
            "Key with spaces: ${spaced key}", "Key with spaces: value with spaces",
            "Concatenated key: ${complexKey}", "Concatenated key: JohnDoe",
            "Multiple replacements: ${firstName} ${lastName}, ${greeting} from ${city}!", "Multiple replacements: John Doe, Hello from New York!"
    );

    @Test
    void testReplaceVariablesBasicReplacement() {
        for (Map.Entry<String, String> e : templates.entrySet()) {
            String result = VariableParser.replaceVariables(e.getKey(), key -> vars.get(key));
            System.out.println(result);
            assertEquals(e.getValue(), result);
        }
    }

}