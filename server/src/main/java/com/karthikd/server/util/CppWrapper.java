package com.karthikd.server.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class CppWrapper {

    public static String wrapCppCode(String userCode, String slug) {
        // Remove function header and footer if exists
        int start = userCode.indexOf('{');
        int end = userCode.lastIndexOf('}');
        if (start != -1 && end != -1 && end > start) {
            userCode = userCode.substring(start + 1, end).trim();
        }

        switch (slug) {
            case "two-sum" -> {
                return String.join("\n",
                        "#include <iostream>",
                        "#include <vector>",
                        "#include <sstream>",
                        "#include <string>",
                        "#include <unordered_map>",
                        "#include <algorithm>",
                        "using namespace std;",
                        "",
                        "vector<int> twoSum(vector<int>& nums, int target) {",
                        "    " + userCode.replace("\n", "\n    "), // indent body
                        "}",
                        "",
                        "int main() {",
                        "    string line;",
                        "    getline(cin, line);",
                        "    vector<int> nums, expected;",
                        "    int target;",
                        "",
                        "    size_t numsPos = line.find(\"nums\");",
                        "    size_t targetPos = line.find(\"target\");",
                        "    size_t expectedPos = line.find(\"expected\");",
                        "",
                        "    size_t start = line.find(\"[\", numsPos);",
                        "    size_t end = line.find(\"]\", start);",
                        "    string numsStr = line.substr(start + 1, end - start - 1);",
                        "    stringstream ss(numsStr);",
                        "    string val;",
                        "    while (getline(ss, val, ',')) {",
                        "        nums.push_back(stoi(val));",
                        "    }",
                        "",
                        "    start = line.find(\":\", targetPos);",
                        "    string targetStr = line.substr(start + 1);",
                        "    targetStr = targetStr.substr(0, targetStr.find(\",\"));",
                        "    target = stoi(targetStr);",
                        "",
                        "    start = line.find(\"[\", expectedPos);",
                        "    end = line.find(\"]\", start);",
                        "    string expectedStr = line.substr(start + 1, end - start - 1);",
                        "    stringstream ess(expectedStr);",
                        "    while (getline(ess, val, ',')) {",
                        "        expected.push_back(stoi(val));",
                        "    }",
                        "",
                        "    vector<int> result = twoSum(nums, target);",
                        "    if (result == expected) {",
                        "        cout << \"Success\" << endl;",
                        "    } else {",
                        "        cout << \"Fail\" << endl;",
                        "        cout << \"Your Output: [\";",
                        "        for (int i = 0; i < result.size(); i++) {",
                        "            cout << result[i];",
                        "            if (i != result.size() - 1) cout << \",\";",
                        "        }",
                        "        cout << \"]\" << endl;",
                        "        cout << \"Expected Output: [\";",
                        "        for (int i = 0; i < expected.size(); i++) {",
                        "            cout << expected[i];",
                        "            if (i != expected.size() - 1) cout << \",\";",
                        "        }",
                        "        cout << \"]\" << endl;",
                        "    }",
                        "    return 0;",
                        "}"
                );
            }
            default -> {
                return userCode;
            }
        }
    }

    public static String wrapCppCodeInput(String input, String expected) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> inputMap = mapper.readValue(input, Map.class);
            inputMap.put("expected", mapper.readValue(expected, Object.class));
            return mapper.writeValueAsString(inputMap);
        } catch (Exception e) {
            e.printStackTrace();
            return input;
        }
    }
}
