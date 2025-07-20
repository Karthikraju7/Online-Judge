package com.karthikd.server.util;

public class PythonWrapper {

    public static String wrapPythonCode(String userCode, String slug) {
        return userCode; // User provides complete code with input/output logic
    }

    public static String wrapPythonCodeInput(String input, String expected) {
        return input; // Raw input, no JSON wrapping
    }
}
