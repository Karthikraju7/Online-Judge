package com.karthikd.server.util;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class FileUtil {

    private static final String CODE_DIR = "codes";

    static {
        new File(CODE_DIR).mkdirs();
    }

    public static Path writeCodeToFile(String code, String extension, String identifier) throws IOException {
        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        String dirPath = CODE_DIR + "/" + safeIdentifier;
        new File(dirPath).mkdirs();

        String fileName = "java".equals(extension) ? "Main.java" : safeIdentifier + "." + extension;
        String filePath = dirPath + "/" + fileName;

        try (FileWriter writer = new FileWriter(filePath, false)) {
            writer.write(code);
        }

        return Paths.get(filePath);
    }

    // Deletes the entire directory for this identifier (code + compiled files)
    public static void cleanupDirectory(String identifier) {
        String safeIdentifier = identifier.replaceAll("[^a-zA-Z0-9]", "_");
        Path dirPath = Paths.get(CODE_DIR, safeIdentifier);
        deleteRecursively(dirPath.toFile());
    }

    private static void deleteRecursively(File file) {
        if (file.isDirectory()) {
            for (File child : file.listFiles()) {
                deleteRecursively(child);
            }
        }
        file.delete();
    }
}