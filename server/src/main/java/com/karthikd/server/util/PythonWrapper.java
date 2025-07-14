package com.karthikd.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class PythonWrapper {

    public static String wrapPythonCode(String userCode, String slug) {
        // Normalize tabs to spaces
        String normalizedUserCode = userCode.replace("\t", "   "); // NO strip()

        boolean hasDef = normalizedUserCode.stripLeading().startsWith("def");

        String functionLine;
        String bodyIndented;

        if (hasDef) {
            String[] lines = normalizedUserCode.split("\n");
            functionLine = lines[0].strip();
            bodyIndented = Arrays.stream(Arrays.copyOfRange(lines, 1, lines.length))
                    .collect(Collectors.joining("\n"));  // No indentation added
        } else {
            functionLine = "def twoSum(nums, target):";
            bodyIndented = Arrays.stream(normalizedUserCode.split("\n"))
                    .map(line -> "    " + line)  // Add indent only if it's a raw body
                    .collect(Collectors.joining("\n"));
        }


        return switch (slug) {
            case "two-sum" -> String.join("\n",
                    functionLine,
                    bodyIndented,
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
