package com.karthikd.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PythonWrapper {

    public static String wrapPythonCode(String userCode, String slug) {
        return switch (slug) {
            case "two-sum" -> String.join("\n",
                    userCode,
                    "",
                    "if __name__ == \"__main__\":",
                    "    import json",
                    "    data = json.loads(input())",
                    "    nums = data[\"nums\"]",
                    "    target = data[\"target\"]",
                    "    expected = data[\"expected\"]",
                    "",
                    "    if isinstance(expected, str):",
                    "        expected = json.loads(expected)",
                    "",
                    "    result = twoSum(nums, target)",
                    "    if result == expected:",
                    "        print(\"Success\")",
                    "    else:",
                    "        print(f\"Fail\\nYour Output: {result}\\nExpected Output: {expected}\")"
            );
            default -> userCode;
        };
    }

    public static String wrapPythonCodeInput(String input, String expected) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> inputMap = mapper.readValue(input, Map.class);
            inputMap.put("expected", mapper.readValue(expected, Object.class));
            return mapper.writeValueAsString(inputMap);
        } catch (Exception e) {
            e.printStackTrace();
            return input; // fallback if anything goes wrong
        }
    }
}
