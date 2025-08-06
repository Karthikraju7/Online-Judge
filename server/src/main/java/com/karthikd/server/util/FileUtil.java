package com.karthikd.server.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class FileUtil {

    private static final String CODE_DIR = "codes";
    private static final String INPUT_DIR = "inputs";

    static {
        new File(CODE_DIR).mkdirs();
        new File(INPUT_DIR).mkdirs();
    }

    public static Path writeCodeToFile(String code, String extension, String identifier) throws IOException {
        String filePath = CODE_DIR + "/" + identifier + "." + extension;
        try (FileWriter writer = new FileWriter(filePath, false)) { // overwrite
            writer.write(code);
        }
        return Paths.get(filePath);
    }

    public static Path writeInputToFile(String input, String identifier) throws IOException {
        Path inputPath = Paths.get("inputs", "input_" + identifier + ".txt");
        Files.writeString(inputPath, input);
        return inputPath;
    }

}

