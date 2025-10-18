package com.karthikd.server.service;

import com.karthikd.server.util.CppExecutor;
import com.karthikd.server.util.FileUtil;
import com.karthikd.server.util.JavaExecutor;
import com.karthikd.server.util.PythonExecutor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

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
    @Value("${GEMINI_API_KEY}")
    private String apiKey;

    public String getAiDebugSuggestion(String code, String output, String problemDescription, String language) {
        try {
            System.out.println("API_KEY = " + apiKey);
            RestTemplate restTemplate = new RestTemplate();
            String modelId = "gemini-2.5-flash";
            System.out.println("Using model: " + modelId);
            String generateUrl = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + modelId + ":generateContent?key=" + apiKey;

            String prompt = """
            You are a coding assistant. The user tried to solve this problem:

            Problem:
            %s

            Language: %s

            Their code:
            %s

            The output they got:
            %s

            Please explain the issue and suggest what to fix. Keep it short and clear.
        """.formatted(problemDescription, language, code, output);

            Map<String, Object> body = Map.of(
                    "contents", new Object[] {
                            Map.of("parts", new Object[] { Map.of("text", prompt) })
                    }
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

            ResponseEntity<Map> response = restTemplate.exchange(generateUrl, HttpMethod.POST, entity, Map.class);
            Map responseBody = response.getBody();

            if (responseBody != null) {
                var candidates = (java.util.List<Map<String, Object>>) responseBody.get("candidates");
                if (candidates != null && !candidates.isEmpty()) {
                    Map<String, Object> content = (Map<String, Object>) candidates.get(0).get("content");
                    if (content != null) {
                        var parts = (java.util.List<Map<String, Object>>) content.get("parts");
                        if (parts != null && !parts.isEmpty()) {
                            return parts.get(0).get("text").toString();
                        }
                    }
                }
            }

            return "No AI suggestion found.";

        } catch (Exception e) {
            e.printStackTrace();
            return "Error calling Gemini AI: " + e.getMessage();
        }
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
