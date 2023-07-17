package com.example.syncapp.util;

import com.example.syncapp.auth.Auth;

import java.util.Random;
import java.util.UUID;

public class IdGenerator {

    private IdGenerator() {

    }

    public static String generate(Auth auth) {
        String id;
        try {
            Thread.sleep(1);
            id = auth.getUserId() + "-" + System.currentTimeMillis();
        } catch (InterruptedException ignored) {
            id = auth.getUserId() + "-" + System.currentTimeMillis() + new Random().nextInt();
        }
        return id;
    }

    @SuppressWarnings("unused")
    private static String generateUUID() {
        return UUID.randomUUID().toString();
    }
}
