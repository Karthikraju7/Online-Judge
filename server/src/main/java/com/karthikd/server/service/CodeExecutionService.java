package com.karthikd.server.service;

import com.karthikd.server.util.CppExecutor;
import com.karthikd.server.util.FileUtil;
import com.karthikd.server.util.JavaExecutor;
import com.karthikd.server.util.PythonExecutor;
import org.springframework.stereotype.Service;

import java.nio.file.Path;

@Service
public class CodeExecutionService {

    public String runCode(String language, String code, String input, String identifier) throws Exception {
        System.out.println("👉 Service Called with Language: " + language + ", Identifier: " + identifier);
        return switch (language.toLowerCase()) {
            case "cpp" -> {
                Path codePath = FileUtil.writeCodeToFile(code, "cpp", identifier);
                Path inputPath = FileUtil.writeInputToFile(input, identifier);
                yield CppExecutor.runCpp(codePath, inputPath);
            }
            case "python" -> PythonExecutor.runPython(code, input, identifier);
            case "java" -> JavaExecutor.runJava(code, input, identifier);
            default -> throw new IllegalArgumentException("Unsupported language: " + language);
        };
    }
}
