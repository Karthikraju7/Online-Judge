package com.karthikd.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CppWrapper {

    public static String wrapCppCode(String userCode, String slug) {
        return userCode; // user writes full code including main, cin, cout
    }

    public static String wrapCppCodeInput(String input, String expected) {
        return input; // pass raw input like "2 7 11 15\n9"
    }
}
