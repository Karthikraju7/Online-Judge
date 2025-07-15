package com.karthikd.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class JavaWrapper {

    public static String wrapJavaCode(String userCode, String slug) {
        int start = userCode.indexOf('{');
        int end = userCode.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            userCode = userCode.substring(start + 1, end).trim();
        }
        switch (slug) {
            case "two-sum" -> {
                return String.join("\n",
                        "import java.util.*;",
                        "import com.fasterxml.jackson.databind.ObjectMapper;",
                        "import java.util.Map;",
                        "",
                        "public class Main {",
                        "    public static int[] twoSum(int[] nums, int target) {",
                        userCode,
                        "    }",
                        "",
                        "    public static void main(String[] args) {",
                        "        try {",
                        "            Scanner sc = new Scanner(System.in);",
                        "            String json = sc.nextLine();",
                        "            ObjectMapper mapper = new ObjectMapper();",
                        "            Map<String, Object> data = mapper.readValue(json, Map.class);",
                        "",
                        "            List<Integer> numsList = (List<Integer>) data.get(\"nums\");",
                        "            int[] nums = numsList.stream().mapToInt(i -> i).toArray();",
                        "            int target = (int) data.get(\"target\");",
                        "            Object expectedRaw = data.get(\"expected\");",
                        "",
                        "            int[] result = twoSum(nums, target);",
                        "            int[] expected = ((List<Integer>) expectedRaw).stream().mapToInt(i -> i).toArray();",
                        "",
                        "            if (Arrays.equals(result, expected)) {",
                        "                System.out.println(\"Success\");",
                        "            } else {",
                        "                System.out.println(\"Fail\");",
                        "                System.out.println(\"Your Output: \" + Arrays.toString(result));",
                        "                System.out.println(\"Expected Output: \" + Arrays.toString(expected));",
                        "            }",
                        "        } catch (Exception e) {",
                        "            System.out.println(\"Error parsing input: \" + e.getMessage());",
                        "        }",
                        "    }",
                        "}"
                );
            }
            default -> {
                return userCode;
            }
        }

    }

    public static String wrapJavaCodeInput(String input, String expected) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> inputMap = mapper.readValue(input, Map.class);
            inputMap.put("expected", mapper.readValue(expected, Object.class));
            return mapper.writeValueAsString(inputMap);
        } catch (Exception e) {
            e.printStackTrace();
            return input; // fallback
        }
    }
}
