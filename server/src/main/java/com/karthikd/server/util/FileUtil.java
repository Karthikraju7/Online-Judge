package com.karthikd.server.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    private static final String CODE_DIR = "codes";
    private static final String INPUT_DIR = "inputs";

    static {
        new File(CODE_DIR).mkdirs();
        new File(INPUT_DIR).mkdirs();
    }

    public static Path writeCodeToFile(String code, String extension, String identifier) throws IOException {
        String dirPath = CODE_DIR + "/" + identifier;
        new File(dirPath).mkdirs(); // ✅ Create user-specific subfolder

        String fileName;
        if ("java".equals(extension)) {
            fileName = "Main.java"; // ✅ For Java, use Main.java inside unique folder
        } else {
            fileName = identifier + "." + extension; // C++ and Python: no restriction
        }

        String filePath = dirPath + "/" + fileName;
        try (FileWriter writer = new FileWriter(filePath, false)) {
            writer.write(code);
        }
        return Paths.get(filePath);
    }

    public static Path writeInputToFile(String input, String identifier) throws IOException {
        Path inputPath = Paths.get(INPUT_DIR, "input_" + identifier + ".txt");
        Files.writeString(inputPath, input);
        return inputPath;
    }
}
