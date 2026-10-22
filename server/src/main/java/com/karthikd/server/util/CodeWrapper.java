package com.karthikd.server.util;

public class CodeWrapper {
    public static String wrap(String code, String language, String slug) {
        return switch (language.toLowerCase()) {
            case "cpp", "java", "python" -> code;
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }
}