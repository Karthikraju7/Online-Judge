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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;

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
            String output = executionService.runCode(language, finalCode, input, identifier).trim();

            Problem problem = problemRepository.findBySlug(slug);
            String expected = problem.getSampleOutput().trim();

            String verdict = output.equals(expected) ? "✅ Correct" : "❌ Incorrect";

            return Map.of("output", output, "verdict", verdict);
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
        List<String> wrappedInputs = new ArrayList<>();

        for (TestCase test : problem.getHiddenTestCases()) {
            String input = switch (language) {
                case "python" -> wrapPythonCodeInput(test.getInput(), test.getExpectedOutput());
                case "cpp" -> wrapCppCodeInput(test.getInput(), test.getExpectedOutput());
                case "java" -> wrapJavaCodeInput(test.getInput(), test.getExpectedOutput());
                default -> test.getInput();
            };

            wrappedInputs.add(input);
            String output = executionService.runCode(language, finalCode, input, identifier).trim();

            if (output.equals(test.getExpectedOutput().trim())) passed++;
        }

        if (passed == total) {
            User user = userRepository.findByEmail(email).orElseThrow();
            userProblemRepository.findByUserAndProblem(user, problem)
                    .orElseGet(() -> userProblemRepository.save(new UserProblem(user, problem, true)));
        }

        String verdict = passed == total
                ? "✅ Accepted"
                : "❌ " + passed + "/" + total + " test cases passed";

        return Map.of("verdict", verdict);
    }


}

