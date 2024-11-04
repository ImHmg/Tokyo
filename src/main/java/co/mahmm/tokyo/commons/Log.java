package co.mahmm.tokyo.commons;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Log {

    public static void info(Object message, Object... params) {
        log.info(String.valueOf(message), params);
    }

    public static void debug(Object message, Object... params) {
        log.debug(String.valueOf(message), params);
    }

    public static void error(Object message, Object... params) {
        log.debug(String.valueOf(message), params);
    }

    public static void info(Object message) {
        log.info(String.valueOf(message));
    }

    public static void debug(Object message) {
        log.debug(String.valueOf(message));
    }

    public static void error(Object message) {
        log.debug(String.valueOf(message));
    }

}
