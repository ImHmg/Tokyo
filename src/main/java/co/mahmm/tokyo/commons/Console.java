package co.mahmm.tokyo.commons;

public class Console {
    public static void print(String...text) {
        StringBuilder s = new StringBuilder();
        for (String s1 : text) {
            s.append(s1);
        }
        System.out.println(s.toString());
    }
}
