package com.karthikd.server.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
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

    // For cpp and py
    public static Path writeCodeToFile(String code, String extension) throws IOException {
        String fileName = UUID.randomUUID().toString();
        String filePath = CODE_DIR + "/" + fileName + "." + extension;
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(code);
        }
        return Paths.get(filePath);
    }

    //For Java
    public static Path writeCodeToFile(String code, String extension, String className) throws IOException {
        String filePath = CODE_DIR + "/" + className + "." + extension;
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(code);
        }
        return Paths.get(filePath);
    }

    public static Path writeInputToFile(String input) throws IOException {
        String fileName = UUID.randomUUID().toString();
        String filePath = INPUT_DIR + "/" + fileName + ".txt";
        try (FileWriter writer = new FileWriter(filePath)) {
            writer.write(input);
        }
        return Paths.get(filePath);
    }
}
