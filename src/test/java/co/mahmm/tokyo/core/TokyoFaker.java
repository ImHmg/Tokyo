package co.mahmm.tokyo.core;

import co.mahmm.tokyo.commons.Log;
import net.datafaker.Faker;

public class TokyoFaker {

    private static Faker faker = new Faker();

    public static String get(String exp) {
        if(exp.startsWith("faker ")) {
            Log.debug("Eval faker expression for  = {}", exp);
            return evalExpression(exp);
        }
        return null;
    }

    private static String evalExpression(String exp) {
        exp = exp.replaceFirst("faker ", "");
        exp = "#{" + exp + "}";
        return faker.expression(exp);
    }
}