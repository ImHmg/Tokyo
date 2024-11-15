package io.github.imhmg.tokyo.commons;

import io.github.imhmg.tokyo.core.TokyoFaker;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

public class VariableParser {

    @FunctionalInterface
    public static interface ValueProvider {
        public String getValue(String key);
    }

    public static String replaceVariables(String content, ValueProvider provider) {
        return replaceVariables(content, provider, 5);
    }

    private static String replaceVariables(String content, ValueProvider provider, int round) {
        if (round <= 0) {
            Log.debug("Populate recursive round limit exceed");
            return content;
        }
        List<String> variables = parseVariables(content);
        if (variables.size() == 0) {
            Log.debug("No variables found to populate");
            return content;
        }
        for (String key : variables) {
            String value = TokyoFaker.get(key);
            if (value == null) {
                value = getPrompt(key);
            }
            if (value == null) {
                value = provider.getValue(key);
            }
            if (value != null) {
                content = content.replace("${" + key + "}", value);
            } else {
                Log.debug("Cannot find variable value for : {}", key);
            }
        }
        round--;
        return replaceVariables(content, provider, round);
    }

    public static List<String> parseVariables(String text) {
        List<String> variables = new ArrayList<>();
        int length = text.length();
        StringBuilder currentVariable = new StringBuilder();
        boolean insideVariable = false;
        int braceCount = 0;
        for (int i = 0; i < length; i++) {
            char currentChar = text.charAt(i);

            if (currentChar == '$' && i + 1 < length && text.charAt(i + 1) == '{') {
                insideVariable = true;
                braceCount = 1;
                i++;
                currentVariable.setLength(0);
            } else if (insideVariable) {
                if (currentChar == '{') {
                    braceCount++;
                } else if (currentChar == '}') {
                    braceCount--;
                    if (braceCount == 0) {
                        variables.add(currentVariable.toString().trim());
                        insideVariable = false;
                    }
                }
                if (braceCount > 0) {
                    currentVariable.append(currentChar);
                }
            }
        }
        return variables;
    }

    private static String getPrompt(String key) {
        if (key == null) {
            return null;
        }
        if (!key.startsWith("prompt ")) {
            return null;
        }
        String text = key.replace("prompt ", "");
        String name = JOptionPane.showInputDialog(null, text, JOptionPane.QUESTION_MESSAGE);
        return name;
    }

}
