package com.karthikd.server.controller;

import com.karthikd.server.entity.Problem;
import com.karthikd.server.entity.TestCase;
import com.karthikd.server.entity.User;
import com.karthikd.server.entity.UserProblem;
import com.karthikd.server.repository.ProblemRepository;
import com.karthikd.server.repository.UserProblemRepository;
import com.karthikd.server.repository.UserRepository;
import com.karthikd.server.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static com.karthikd.server.util.CppWrapper.wrapCppCode;
import static com.karthikd.server.util.CppWrapper.wrapCppCodeInput;
import static com.karthikd.server.util.JavaWrapper.wrapJavaCode;
import static com.karthikd.server.util.JavaWrapper.wrapJavaCodeInput;
import static com.karthikd.server.util.PythonWrapper.wrapPythonCode;
import static com.karthikd.server.util.PythonWrapper.wrapPythonCodeInput;

@RestController
@RequestMapping("/problems")
@CrossOrigin(origins = "*") // for frontend calls
public class CodeExecutionController {

    @Autowired
    private CodeExecutionService executionService;
    @Autowired
    private ProblemRepository problemRepository;
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserProblemRepository userProblemRepository;

    @PostMapping("/run")
    public Map<String, String> runCode(@RequestBody Map<String, String> request) {
        try {
            String rawCode = request.get("code");
            String input = request.get("input");
            String language = request.get("language");
            String slug = request.get("slug");

            String finalCode = switch (language) {
                case "python" -> wrapPythonCode(rawCode, slug);
                case "cpp" -> wrapCppCode(rawCode, slug);
                case "java" -> wrapJavaCode(rawCode, slug);
                default -> rawCode;
            };

            String identifier = "temp_user_" + slug + "_" + language;
            Map<String, String> result = executionService.runCode(language, finalCode, input, identifier);

            String output = result.get("output").trim();
            String timeUsedStr = result.getOrDefault("timeUsed", "-").replace("ms", "").trim();
            String memoryUsedStr = result.getOrDefault("memoryUsed", "-").replace("KB", "").trim();

            long timeUsed = 0;
            long memoryUsed = 0;
            try {
                timeUsed = Long.parseLong(timeUsedStr);
                memoryUsed = Long.parseLong(memoryUsedStr);

                if (timeUsed < 0) {
                    return Map.of(
                            "output", "❌ Time Limit Exceeded",
                            "verdict", "❌ TLE"
                    );
                }

                if (memoryUsed < 0) {
                    return Map.of(
                            "output", "❌ Memory Limit Exceeded",
                            "verdict", "❌ MLE"
                    );
                }
            } catch (NumberFormatException e) {
                return Map.of(
                        "output", "❌ Invalid resource output: " + e.getMessage(),
                        "verdict", "❌ Error"
                );
            }


            Problem problem = problemRepository.findBySlug(slug);
            String expected = problem.getSampleOutput().trim();

            String verdict = output.equals(expected) ? "✅ Correct" : "❌ Incorrect";

            return Map.of(
                    "output", output,
                    "verdict", verdict,
                    "timeUsed", timeUsed + "ms",
                    "memoryUsed", memoryUsed + "KB"
            );
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of(
                    "output", "❌ Server error: " + e.getMessage(),
                    "verdict", "❌ Error",
                    "timeUsed", "0",
                    "memoryUsed", "0"
            );
        }
    }


    @PostMapping("/submit")
    public Map<String, Object> submitCode(@RequestBody Map<String, String> request) throws Exception {
        String code = request.get("code");
        String language = request.get("language");
        String slug = request.get("slug");
        String email = request.get("email");

        Problem problem = problemRepository.findBySlug(slug);
        if (problem == null) return Map.of("verdict", "❌ Problem not found");

        int passed = 0;
        int total = problem.getHiddenTestCases().size();

        String finalCode = switch (language) {
            case "python" -> wrapPythonCode(code, slug);
            case "cpp" -> wrapCppCode(code, slug);
            case "java" -> wrapJavaCode(code, slug);
            default -> code;
        };

        String identifier = email + "_" + slug + "_" + language;

        for (TestCase test : problem.getHiddenTestCases()) {
            String input = switch (language) {
                case "python" -> wrapPythonCodeInput(test.getInput(), test.getExpectedOutput());
                case "cpp" -> wrapCppCodeInput(test.getInput(), test.getExpectedOutput());
                case "java" -> wrapJavaCodeInput(test.getInput(), test.getExpectedOutput());
                default -> test.getInput();
            };

            Map<String, String> result = executionService.runCode(language, finalCode, input, identifier);

            String output = result.getOrDefault("output", "").trim();
            String timeUsedStr = result.getOrDefault("timeUsed", "-").replace("ms", "").trim();
            String memoryUsedStr = result.getOrDefault("memoryUsed", "-").replace("KB", "").trim();

            long timeUsed = 0;
            long memoryUsed = 0;
            try {
                timeUsed = Long.parseLong(timeUsedStr);
                memoryUsed = Long.parseLong(memoryUsedStr);
            } catch (NumberFormatException e) {
                return Map.of("verdict", "❌ Invalid resource usage output from executor");
            }

            // TLE Check
            if (timeUsed == -1) {
                return Map.of(
                        "verdict", "❌ Time Limit Exceeded at test case " + (passed + 1),
                        "passed", passed,
                        "total", total
                );
            }
            if (timeUsed > 1000) {
                return Map.of(
                        "verdict", "❌ Time Limit Exceeded at test case " + (passed + 1),
                        "passed", passed,
                        "total", total
                );
            }


            // MLE Check
            if (memoryUsed == -1) {
                return Map.of(
                        "verdict", "❌ Memory Limit Exceeded at test case " + (passed + 1),
                        "passed", passed,
                        "total", total
                );
            }
            if (memoryUsed > 262144) {
                return Map.of(
                        "verdict", "❌ Memory Limit Exceeded at test case " + (passed + 1),
                        "passed", passed,
                        "total", total
                );
            }


            // Normalize and compare outputs
            String normalizedOutput = output.replaceAll("\\s+", " ").trim();
            String normalizedExpectedOutput = test.getExpectedOutput().replaceAll("\\s+", " ").trim();

            if (normalizedOutput.equals(normalizedExpectedOutput)) {
                passed++;
            }
        }

        if (passed == total) {
            User user = userRepository.findByEmail(email).orElseThrow();
            userProblemRepository.findByUserAndProblem(user, problem)
                    .orElseGet(() -> userProblemRepository.save(new UserProblem(user, problem, true)));
        }

        String verdict = passed == total
                ? "✅ Accepted"
                : "❌ " + passed + "/" + total + " test cases passed";

        return Map.of(
                "verdict", verdict,
                "passed", passed,
                "total", total
        );
    }

    @PostMapping("/ai/debug")
    public Map<String, String> getAiDebugHelp(@RequestBody Map<String, String> request) {
        try {
            String code = request.get("code");
            String output = request.get("output");
            String problemDescription = request.get("problemDescription");
            String language = request.get("language");

            String suggestion = executionService.getAiDebugSuggestion(code, output, problemDescription, language);

            return Map.of("response", suggestion);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("response", "⚠️ AI failed: " + e.getMessage());
        }
    }

}

