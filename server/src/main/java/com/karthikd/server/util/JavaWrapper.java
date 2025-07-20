package com.karthikd.server.util;

public class JavaWrapper {

    public static String wrapJavaCode(String userCode, String slug) {
        return userCode; // user writes full code including main and Scanner input
    }

    public static String wrapJavaCodeInput(String input, String expected) {
        return input; // raw input, e.g. "4\n2 7 11 15\n9"
    }
}
