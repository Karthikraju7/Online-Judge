package com.karthikd.server.service;

import com.karthikd.server.util.CppExecutor;
import com.karthikd.server.util.FileUtil;
import com.karthikd.server.util.JavaExecutor;
import com.karthikd.server.util.PythonExecutor;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

import java.nio.file.Path;

@Service
public class CodeExecutionService {
    public Map<String, String> runCode(String language, String code, String input, String identifier) throws Exception {
        System.out.println("👉 Service Called with Language: " + language + ", Identifier: " + identifier);
        String result = switch (language.toLowerCase()) {
            case "cpp" -> {
                Path codePath = FileUtil.writeCodeToFile(code, "cpp", identifier);
                Path inputPath = FileUtil.writeInputToFile(input, identifier);
                yield CppExecutor.runCpp(codePath, inputPath);
            }
            case "python" -> PythonExecutor.runPython(code, input, identifier);
            case "java" -> JavaExecutor.runJava(code, input, identifier);
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };

        String output = extractBetween(result, "OUTPUT_START", "OUTPUT_END").trim();
        String timeUsed = extractAfter(result, "TIME:").trim();
        String memoryUsed = extractAfter(result, "MEMORY:").trim();

        Map<String, String> response = new HashMap<>();
        response.put("output", output);
        response.put("timeUsed", timeUsed);
        response.put("memoryUsed", memoryUsed);
        return response;
    }

    private String extractBetween(String text, String startMarker, String endMarker) {
        int start = text.indexOf(startMarker);
        int end = text.indexOf(endMarker);
        if (start == -1 || end == -1 || end < start) return "Unknown";
        return text.substring(start + startMarker.length(), end).trim();
    }

    private String extractAfter(String text, String marker) {
        int start = text.indexOf(marker);
        if (start == -1) return "Unknown";
        return text.substring(start + marker.length()).split("\n")[0].trim();
    }
}
