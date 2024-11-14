package io.github.imhmg.tokyo.commons;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Log {

    public static boolean isDebug = false;

    public static void info(Object message, Object... params) {
        log.info(String.valueOf(message), params);
    }

    public static void debug(Object message, Object... params) {
        if(!isDebug) return;
        log.debug(String.valueOf(message), params);
    }

    public static void error(Object message, Object... params) {
        log.debug(String.valueOf(message), params);
    }

    public static void info(Object message) {
        log.info(String.valueOf(message));
    }

    public static void debug(Object message) {
        if(!isDebug) return;
        log.debug(String.valueOf(message));
    }

    public static void error(Object message) {
        log.debug(String.valueOf(message));
    }

}
