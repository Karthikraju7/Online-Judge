package com.karthikd.server.service;

import com.karthikd.server.util.CppExecutor;
import com.karthikd.server.util.FileUtil;
import com.karthikd.server.util.JavaExecutor;
import com.karthikd.server.util.PythonExecutor;
import org.springframework.stereotype.Service;
import com.karthikd.server.util.FileUtil;

import java.nio.file.Path;

@Service
public class CodeExecutionService {

    public String runCode(String language, String code, String input) throws Exception {
        System.out.println("👉 Service Called with Language: " + language);
        return switch (language.toLowerCase()) {
            case "cpp" -> {
                Path codePath = FileUtil.writeCodeToFile(code, "cpp");
                Path inputPath = FileUtil.writeInputToFile(input);
                yield CppExecutor.runCpp(codePath, inputPath);
            }
            case "python" -> PythonExecutor.runPython(code, input);
            case "java" -> JavaExecutor.runJava(code, input);
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }
}
