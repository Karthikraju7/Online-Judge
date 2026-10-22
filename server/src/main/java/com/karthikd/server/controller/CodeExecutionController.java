package com.karthikd.server.controller;

import com.karthikd.server.entity.Problem;
import com.karthikd.server.entity.TestCase;
import com.karthikd.server.entity.User;
import com.karthikd.server.entity.UserProblem;
import com.karthikd.server.repository.ProblemRepository;
import com.karthikd.server.repository.UserProblemRepository;
import com.karthikd.server.repository.UserRepository;
import com.karthikd.server.service.CodeExecutionService;
import com.karthikd.server.util.CppExecutor;
import com.karthikd.server.util.JavaExecutor;
import com.karthikd.server.util.PythonExecutor;
import com.karthikd.server.util.FileUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

import static com.karthikd.server.util.CodeWrapper.wrap;

@RestController
@RequestMapping("/problems")
@CrossOrigin(origins = "*")
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService executionService;

    @Autowired
    private ProblemRepository problemRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProblemRepository userProblemRepository;

    // RUN — single test case, unchanged
    @PostMapping("/run")
    public Map<String, String> runCode(@RequestBody Map<String, String> request) {
        String identifier = null;
        try {
            String rawCode = request.get("code");
            String input = request.get("input");
            String language = request.get("language");
            String slug = request.get("slug");

            String finalCode = switch (language) {
                case "python" -> wrap(rawCode, "python", slug);
                case "cpp"    -> wrap(rawCode, "cpp", slug);
                case "java"   -> wrap(rawCode, "java", slug);
                default       -> rawCode;
            };

            identifier = "temp_user_" + slug + "_" + language + "_" + UUID.randomUUID();

            Map<String, String> result = executionService.runCode(language, finalCode, input, identifier);

            if (result == null || !result.containsKey("output")) {
                return Map.of("output", "❌ Execution failed", "verdict", "❌ Error");
            }

            String timeStr = result.getOrDefault("timeUsed", "0");
            long timeUsed = parseLong(timeStr, "ms");
            if (timeUsed < 0 || timeUsed > 3000) {
                return Map.of(
                        "output", "❌ Time Limit Exceeded (timeUsed=" + timeStr + ")",
                        "verdict", "❌ Time Limit Exceeded"
                );
            }

            String memStr = result.getOrDefault("memoryUsed", "0");
            long memoryUsed = parseLong(memStr, "KB");
            if (memoryUsed > 0 && memoryUsed > 262144) {
                return Map.of("output", "❌ Memory Limit Exceeded", "verdict", "❌ Memory Limit Exceeded");
            }

            String output = result.get("output").trim();
            Problem problem = problemRepository.findBySlug(slug);
            String expected = problem.getSampleOutput().trim();
            String verdict = output.equals(expected) ? "✅ Correct" : "❌ Incorrect";

            return Map.of("output", output, "verdict", verdict);

        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("output", "❌ Server error: " + e.getMessage(), "verdict", "❌ Error");
        } finally {
            if (identifier != null) FileUtil.cleanupDirectory(identifier);
        }
    }

    // SUBMIT — batch execution for all languages
    @PostMapping("/submit")
    public Map<String, Object> submitCode(@RequestBody Map<String, String> request) throws Exception {

        String code = request.get("code");
        String language = request.get("language");
        String slug = request.get("slug");
        String email = request.get("email");

        Problem problem = problemRepository.findBySlug(slug);
        if (problem == null) return Map.of("verdict", "❌ Problem not found");

        List<TestCase> testCases = problem.getHiddenTestCases();
        int total = testCases.size();
        int passed = 0;

        String finalCode = switch (language) {
            case "python" -> wrap(code, "python", slug);
            case "cpp"    -> wrap(code, "cpp", slug);
            case "java"   -> wrap(code, "java", slug);
            default       -> code;
        };

        String identifier = email + "_" + slug + "_" + language + "_" + UUID.randomUUID();

        try {
            // Normalize inputs once
            List<String> inputs = testCases.stream()
                    .map(tc -> tc.getInput().endsWith("\n") ? tc.getInput() : tc.getInput() + "\n")
                    .collect(Collectors.toList());

            // COMPILE ONCE + BATCH RUN — one container startup for all test cases
            List<String> results = switch (language) {
                case "cpp" -> {
                    long t1 = System.currentTimeMillis();
                    Object compiled = executionService.compile("cpp", finalCode, identifier);
                    System.out.println("COMPILE TIME: " + (System.currentTimeMillis() - t1) + "ms");

                    long t2 = System.currentTimeMillis();
                    List<String> r = CppExecutor.runAllTestCases((File) compiled, inputs, identifier);
                    System.out.println("RUN ALL TIME: " + (System.currentTimeMillis() - t2) + "ms");
                    yield r;
                }
                case "java" -> {
                    Object compiled = executionService.compile("java", finalCode, identifier);
                    yield JavaExecutor.runAllTestCases((File) compiled, inputs, identifier);
                }
                case "python" -> PythonExecutor.runAllTestCases(finalCode, inputs, identifier);
                default -> throw new IllegalArgumentException("Unsupported language");
            };

            // EVALUATE
            for (int i = 0; i < testCases.size(); i++) {
                String raw = results.get(i);
                TestCase test = testCases.get(i);

                String output = extractBetween(raw, "OUTPUT_START", "OUTPUT_END").trim();
                long timeUsed = parseLong(extractAfter(raw, "TIME:"), "ms");
                long memoryUsed = parseLong(extractAfter(raw, "MEMORY:"), "KB");

                if (timeUsed < 0 || timeUsed > 3000) {
                    return Map.of(
                            "verdict", "❌ Time Limit Exceeded at test case " + (i + 1),
                            "passed", passed, "total", total
                    );
                }

                if (memoryUsed > 0 && memoryUsed > 262144) {
                    return Map.of(
                            "verdict", "❌ Memory Limit Exceeded at test case " + (i + 1),
                            "passed", passed, "total", total
                    );
                }

                List<String> outTokens = Arrays.stream(output.trim().split("\\s+"))
                        .filter(s -> !s.isEmpty()).toList();
                List<String> expTokens = Arrays.stream(test.getExpectedOutput().trim().split("\\s+"))
                        .filter(s -> !s.isEmpty()).toList();

                if (!outTokens.equals(expTokens)) {
                    return Map.of(
                            "verdict", "❌ Wrong Answer at test case " + (i + 1),
                            "passed", passed, "total", total
                    );
                }

                passed++;
            }

        } finally {
            FileUtil.cleanupDirectory(identifier);
        }

        // Save accepted
        if (passed == total) {
            User user = userRepository.findByEmail(email).orElseThrow();
            userProblemRepository.findByUserAndProblem(user, problem)
                    .orElseGet(() -> userProblemRepository.save(new UserProblem(user, problem, true)));
        }

        return Map.of("verdict", "✅ Accepted", "passed", passed, "total", total);
    }

    // AI DEBUG — unchanged
    @PostMapping("/ai/debug")
    public Map<String, String> getAiDebugHelp(@RequestBody Map<String, String> request) {
        try {
            String suggestion = executionService.getAiDebugSuggestion(
                    request.get("code"),
                    request.get("output"),
                    request.get("problemDescription"),
                    request.get("language")
            );
            return Map.of("response", suggestion);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("response", "⚠️ AI failed: " + e.getMessage());
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

    private long parseLong(String value, String suffix) {
        try {
            return Long.parseLong(value.replace(suffix, "").trim());
        } catch (NumberFormatException e) {
            return -1L;
        }
    }
}