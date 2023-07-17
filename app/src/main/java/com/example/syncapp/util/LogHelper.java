package com.example.syncapp.util;

public class LogHelper {
    public static String tag(Object o) {
        return "Raghu-" + o.getClass().getSimpleName();
    }

    public static String tag(Class<?> c) {
        return "Raghu-" + c.getSimpleName();
    }
}
