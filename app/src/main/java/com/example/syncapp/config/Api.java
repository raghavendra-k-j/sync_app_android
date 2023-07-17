package com.example.syncapp.config;

import java.util.HashMap;
import java.util.Map;

public class Api {
    private static final String ROOT_URL = "http://192.168.43.64:8080";

    public static String getRootUrl(String path) {
        return path.startsWith("/") ? ROOT_URL + path : ROOT_URL + "/" + path;
    }

    public static String getRootUrl(String path, HashMap<String, String> params) {
        StringBuilder urlBuilder = new StringBuilder();
        urlBuilder.append(path.startsWith("/") ? ROOT_URL + path : ROOT_URL + "/" + path);
        if (params != null && !params.isEmpty()) {
            urlBuilder.append("?");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                String key = entry.getKey();
                String value = entry.getValue();
                urlBuilder.append(key).append("=").append(value).append("&");
            }
            urlBuilder.deleteCharAt(urlBuilder.length() - 1);
        }
        return urlBuilder.toString();
    }
}
