package com.karthikd.server.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.karthikd.server.entity.Problem;
import com.karthikd.server.entity.TestCase;
import com.karthikd.server.repository.ProblemRepository;
import com.karthikd.server.service.CodeExecutionService;
import org.springframework.beans.factory.annotation.Autowired;
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

    @PostMapping("/run")
    public Map<String, String> runCode(@RequestBody Map<String, String> request) {
        try {
            String rawCode = request.get("code");
            String input = request.get("input");
            System.out.println("Received input: " + input);
            String language = request.get("language");
            String slug = request.get("slug");

            String finalCode = switch (language) {
                case "python" -> wrapPythonCode(rawCode, slug);
                case "cpp" -> wrapCppCode(rawCode, slug);
                case "java" -> wrapJavaCode(rawCode, slug); // ✅ add this
                default -> rawCode;
            };



            System.out.println("🛠️ Run Request Received:");
            System.out.println("Language: " + language);
            System.out.println("Input: " + input);
            System.out.println("Code:\n" + finalCode);

            String output = executionService.runCode(language, finalCode, input);
            return Map.of("output", output);
        } catch (Exception e) {
            e.printStackTrace();
            return Map.of("output", "❌ Server error: " + e.getMessage());
        }
    }

    @PostMapping("/submit")
    public Map<String, Object> submitCode(@RequestBody Map<String, String> request) throws Exception {
        String code = request.get("code");
        String language = request.get("language");
        String slug = request.get("slug");

        Problem problem = problemRepository.findBySlug(slug);
        if (problem == null) {
            return Map.of("verdict", "❌ Problem not found");
        }

        int passed = 0;
        int total = problem.getHiddenTestCases().size();

        // Wrap code once for Python
        String finalCode = switch (language) {
            case "python" -> wrapPythonCode(code, slug);
            case "cpp" -> wrapCppCode(code, slug);
            case "java" -> wrapJavaCode(code, slug);
            default -> code;
        };


        // ✅ Collect test inputs for debugging
        List<String> wrappedInputs = new ArrayList<>();

        for (TestCase test : problem.getHiddenTestCases()) {
            String input = switch (language) {
                case "python" -> wrapPythonCodeInput(test.getInput(), test.getExpectedOutput());
                case "cpp" -> wrapCppCodeInput(test.getInput(), test.getExpectedOutput());
                case "java" -> wrapJavaCodeInput(test.getInput(), test.getExpectedOutput());
                default -> test.getInput();
            };

            wrappedInputs.add(input); // 🧠 collect input for frontend

            System.out.println("🔍 Test Input: " + input);
            System.out.println("🔍 Expected Output: " + test.getExpectedOutput());

            String output = executionService.runCode(language, finalCode, input).trim();
            System.out.println("🔁 Actual Output: " + output);

            if (output.contains("Success")) {
                passed++;
            } else{
                break;
            }
        }

        String verdict = passed == total
                ? "✅ Accepted"
                : "❌ " + passed + "/" + total + " test cases passed";

        // 🧪 Return inputs and verdict to frontend
        return Map.of(
                "verdict", verdict,
                "inputs", wrappedInputs
        );
    }
}

