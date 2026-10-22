package com.karthikd.server.util;

import lombok.extern.slf4j.Slf4j;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;

@Slf4j
public class WarmContainerManager {

    public enum Lang { CPP, JAVA, PYTHON }

    private static final String CPP_IMAGE    = "judge-cpp";
    private static final String JAVA_IMAGE   = "judge-java";
    private static final String PYTHON_IMAGE = "judge-python";

    private static final String CPP_NAME    = "judge_warm_cpp";
    private static final String JAVA_NAME   = "judge_warm_java";
    private static final String PYTHON_NAME = "judge_warm_python";

    private static final String HOST_CODES_DIR = "codes";
    private static final String CONTAINER_WORK_DIR = "/code";
    private static final int TIME_LIMIT_SEC = 3;
    private static final long CONTAINER_MEMORY_KB = 512 * 1024;
    private static final int MAX_OUTPUT_CHARS = 1_000_000;
    private static final String SKIPPED = "OUTPUT_START\nSkipped\nOUTPUT_END\nTIME:0ms\nMEMORY:0KB";

    private static final Object[] LOCKS = { new Object(), new Object(), new Object() };
    private static final Semaphore[] SLOTS = { new Semaphore(2), new Semaphore(2), new Semaphore(2) };

    private static volatile String cppContainerId    = null;
    private static volatile String javaContainerId   = null;
    private static volatile String pythonContainerId = null;

    // Lifecycle

    public static synchronized void initialize() throws IOException, InterruptedException {
        startLang(Lang.CPP);
        startLang(Lang.JAVA);
        startLang(Lang.PYTHON);

        log.info("Warm containers started — cpp: {}, java: {}, python: {}",
                cppContainerId, javaContainerId, pythonContainerId);
    }

    public static synchronized void shutdown() {
        killExisting(CPP_NAME);
        killExisting(JAVA_NAME);
        killExisting(PYTHON_NAME);
        cppContainerId = javaContainerId = pythonContainerId = null;
        log.info("Warm containers stopped.");
    }

    // Compile

    public static void compileCpp(String safeIdentifier, String codeFileName, String exeFileName)
            throws IOException, InterruptedException {
        String srcPath = CONTAINER_WORK_DIR + "/" + safeIdentifier + "/" + codeFileName;
        String outPath = CONTAINER_WORK_DIR + "/" + safeIdentifier + "/" + exeFileName;
        try {
            withSlot(Lang.CPP, () -> {
                execCompile(getContainerId(Lang.CPP), "g++", "-std=c++17", srcPath, "-o", outPath);
                return null;
            });
        } catch (IOException | InterruptedException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public static void compileJava(String safeIdentifier, String javaFileName)
            throws IOException, InterruptedException {
        String srcPath = CONTAINER_WORK_DIR + "/" + safeIdentifier + "/" + javaFileName;
        try {
            withSlot(Lang.JAVA, () -> {
                execCompile(getContainerId(Lang.JAVA), "javac", srcPath);
                return null;
            });
        } catch (IOException | InterruptedException | RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Run (single input)

    public static String runCpp(String safeIdentifier, String exeFileName, String input) throws Exception {
        String binPath = CONTAINER_WORK_DIR + "/" + safeIdentifier + "/" + exeFileName;
        return withSlot(Lang.CPP, () -> execRun(getContainerId(Lang.CPP), safeIdentifier, input, binPath));
    }

    public static String runJava(String safeIdentifier, String className, String input) throws Exception {
        String workDir = CONTAINER_WORK_DIR + "/" + safeIdentifier;
        return withSlot(Lang.JAVA, () -> execRun(getContainerId(Lang.JAVA), safeIdentifier, input,
                "java", "-Xmx256m", "-cp", workDir, className));
    }

    public static String runPython(String safeIdentifier, String pyFileName, String input) throws Exception {
        String scriptPath = CONTAINER_WORK_DIR + "/" + safeIdentifier + "/" + pyFileName;
        return withSlot(Lang.PYTHON, () -> execRun(getContainerId(Lang.PYTHON), safeIdentifier, input,
                "python3", scriptPath));
    }

    // Run (all test cases in one docker exec)

    public static List<String> runCppBatch(String safeIdentifier, String exeFileName, List<String> inputs)
            throws Exception {
        String binPath = CONTAINER_WORK_DIR + "/" + safeIdentifier + "/" + exeFileName;
        return withSlot(Lang.CPP, () -> execBatch(getContainerId(Lang.CPP), safeIdentifier, inputs, binPath));
    }

    public static List<String> runJavaBatch(String safeIdentifier, String className, List<String> inputs)
            throws Exception {
        String workDir = CONTAINER_WORK_DIR + "/" + safeIdentifier;
        return withSlot(Lang.JAVA, () -> execBatch(getContainerId(Lang.JAVA), safeIdentifier, inputs,
                "java", "-Xmx256m", "-cp", workDir, className));
    }

    public static List<String> runPythonBatch(String safeIdentifier, String pyFileName, List<String> inputs)
            throws Exception {
        String scriptPath = CONTAINER_WORK_DIR + "/" + safeIdentifier + "/" + pyFileName;
        return withSlot(Lang.PYTHON, () -> execBatch(getContainerId(Lang.PYTHON), safeIdentifier, inputs,
                "python3", scriptPath));
    }

    // Internal

    private static <T> T withSlot(Lang lang, Callable<T> task) throws Exception {
        Semaphore s = SLOTS[lang.ordinal()];
        s.acquire();
        try {
            return task.call();
        } finally {
            s.release();
        }
    }

    private static String currentId(Lang lang) {
        return switch (lang) {
            case CPP    -> cppContainerId;
            case JAVA   -> javaContainerId;
            case PYTHON -> pythonContainerId;
        };
    }

    private static String nameOf(Lang lang) {
        return switch (lang) {
            case CPP    -> CPP_NAME;
            case JAVA   -> JAVA_NAME;
            case PYTHON -> PYTHON_NAME;
        };
    }

    private static String imageOf(Lang lang) {
        return switch (lang) {
            case CPP    -> CPP_IMAGE;
            case JAVA   -> JAVA_IMAGE;
            case PYTHON -> PYTHON_IMAGE;
        };
    }

    private static void startLang(Lang lang) throws IOException, InterruptedException {
        String hostCodesDir = new File(HOST_CODES_DIR).getAbsolutePath();
        killExisting(nameOf(lang));
        String id = startContainer(nameOf(lang), imageOf(lang), hostCodesDir);
        switch (lang) {
            case CPP    -> cppContainerId = id;
            case JAVA   -> javaContainerId = id;
            case PYTHON -> pythonContainerId = id;
        }
    }

    private static String getContainerId(Lang lang) {
        String id = currentId(lang);
        if (id != null && !id.isBlank()) return id;

        synchronized (LOCKS[lang.ordinal()]) {
            id = currentId(lang);
            if (id == null || id.isBlank()) {
                try {
                    log.warn("{} warm container not running, restarting...", lang);
                    startLang(lang);
                } catch (Exception e) {
                    throw new RuntimeException("Failed to restart warm container for " + lang, e);
                }
            }
            return currentId(lang);
        }
    }

    private static String startContainer(String name, String image, String hostCodesDir)
            throws IOException, InterruptedException {

        ProcessBuilder pb = new ProcessBuilder(
                "docker", "run", "-d",
                "--name", name,
                "--memory", "512m",
                "--cpus", "2",
                "--network", "none",
                "-v", hostCodesDir + ":" + CONTAINER_WORK_DIR,
                "-w", CONTAINER_WORK_DIR,
                image,
                "sleep", "infinity"
        );
        pb.redirectErrorStream(true);
        Process process = pb.start();

        String id = new BufferedReader(new InputStreamReader(process.getInputStream())).readLine();
        int exit = process.waitFor();

        if (exit != 0 || id == null || id.isBlank()) {
            throw new RuntimeException("Failed to start warm container: " + name);
        }
        return id.trim();
    }

    private static void execCompile(String containerId, String... cmd)
            throws IOException, InterruptedException {

        String[] fullCmd = new String[cmd.length + 3];
        fullCmd[0] = "docker";
        fullCmd[1] = "exec";
        fullCmd[2] = containerId;
        System.arraycopy(cmd, 0, fullCmd, 3, cmd.length);

        ProcessBuilder pb = new ProcessBuilder(fullCmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder errors = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) errors.append(line).append("\n");
        }

        int exit = process.waitFor();
        if (exit != 0) throw new RuntimeException("Compilation Error:\n" + errors);
    }

    // Single run

    private static String execRun(String containerId, String safeIdentifier, String input, String... cmd)
            throws Exception {
        File memFile = new File(HOST_CODES_DIR + "/" + safeIdentifier, "mem.txt");
        memFile.delete();
        long startTime = System.nanoTime();

        String[] prefix = {
                "docker", "exec", "-i", containerId,
                "/usr/bin/time", "-f", "%M", "-o", CONTAINER_WORK_DIR + "/" + safeIdentifier + "/mem.txt",
                "timeout", "-k", "1", String.valueOf(TIME_LIMIT_SEC)
        };
        String[] execCmd = new String[prefix.length + cmd.length];
        System.arraycopy(prefix, 0, execCmd, 0, prefix.length);
        System.arraycopy(cmd, 0, execCmd, prefix.length, cmd.length);

        ProcessBuilder pb = new ProcessBuilder(execCmd);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        StringBuilder output = new StringBuilder();
        Thread reader = new Thread(() -> {
            try (BufferedReader r = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = r.readLine()) != null) {
                    if (output.length() < MAX_OUTPUT_CHARS) output.append(line).append("\n");
                }
            } catch (IOException ignored) {}
        });
        reader.start();

        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(process.getOutputStream()))) {
            writer.write(input);
            writer.flush();
        } catch (IOException ignored) {}

        boolean finished = process.waitFor(TIME_LIMIT_SEC + 5, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            return buildResult(124, TIME_LIMIT_SEC * 1000L, -1, "");
        }
        reader.join(1000);

        long timeUsedMs = (System.nanoTime() - startTime) / 1_000_000;
        return buildResult(process.exitValue(), timeUsedMs, readPeakKb(memFile), output.toString().trim());
    }

    // Batch run

    private static List<String> execBatch(String containerId, String safeIdentifier, List<String> inputs,
                                          String... cmd) throws Exception {
        int n = inputs.size();
        File dir = new File(HOST_CODES_DIR, safeIdentifier);
        dir.mkdirs();

        for (int i = 0; i < n; i++) {
            Files.writeString(new File(dir, "in_" + i + ".txt").toPath(), inputs.get(i), StandardCharsets.UTF_8);
        }

        StringBuilder quoted = new StringBuilder();
        for (String c : cmd) quoted.append(shellQuote(c)).append(' ');

        String work = CONTAINER_WORK_DIR + "/" + safeIdentifier;
        String script = """
                ulimit -f 65536
                cd %s
                i=0
                while [ $i -lt %d ]; do
                  s=$(date +%%s%%N)
                  /usr/bin/time -f "%%M" -o mem_$i.txt timeout -k 1 %d %s< in_$i.txt > out_$i.txt 2>&1
                  c=$?
                  e=$(date +%%s%%N)
                  echo $c > code_$i.txt
                  echo $(( (e - s) / 1000000 )) > ms_$i.txt
                  if [ $c -ne 0 ]; then break; fi
                  i=$((i+1))
                done
                """.formatted(work, n, TIME_LIMIT_SEC, quoted);
        Files.writeString(new File(dir, "run_all.sh").toPath(), script, StandardCharsets.UTF_8);

        ProcessBuilder pb = new ProcessBuilder("docker", "exec", containerId, "sh", work + "/run_all.sh");
        pb.redirectErrorStream(true);
        Process process = pb.start();

        Thread drain = new Thread(() -> {
            try {
                process.getInputStream().transferTo(OutputStream.nullOutputStream());
            } catch (IOException ignored) {}
        });
        drain.start();

        long maxWaitSec = (long) n * (TIME_LIMIT_SEC + 2) + 15;
        boolean finished = process.waitFor(maxWaitSec, TimeUnit.SECONDS);
        if (!finished) {
            process.destroyForcibly();
            List<String> timedOut = new ArrayList<>();
            for (int i = 0; i < n; i++) timedOut.add(buildResult(124, TIME_LIMIT_SEC * 1000L, -1, ""));
            return timedOut;
        }
        drain.join(1000);

        if (n > 0 && !new File(dir, "code_0.txt").exists()) {
            throw new RuntimeException("Batch execution failed: no results produced");
        }

        List<String> results = new ArrayList<>(n);
        boolean stopped = false;
        for (int i = 0; i < n; i++) {
            File codeFile = new File(dir, "code_" + i + ".txt");
            if (stopped || !codeFile.exists()) {
                results.add(SKIPPED);
                stopped = true;
                continue;
            }
            int code = (int) readLong(codeFile, -1);
            long ms = readLong(new File(dir, "ms_" + i + ".txt"), 0);
            long mem = readPeakKb(new File(dir, "mem_" + i + ".txt"));
            String out = readOutput(new File(dir, "out_" + i + ".txt"));
            results.add(buildResult(code, ms, mem, out));
            if (code != 0 || out.isEmpty()) {
                log.warn("Batch case {} in {} failed: exit={}, output={}", i, safeIdentifier, code, out);
            }
            if (code != 0) stopped = true;
        }
        return results;
    }

    // Result formatting

    private static String buildResult(int exitCode, long timeMs, long memKb, String out) {
        boolean timedOut = exitCode == 124 || (exitCode != 0 && timeMs >= TIME_LIMIT_SEC * 1000L);
        if (timedOut) {
            return String.format(
                    "OUTPUT_START\n❌ Time Limit Exceeded\nOUTPUT_END\nTIME:9999ms\nMEMORY:%dKB", memKb);
        }
        if (exitCode == 9 || exitCode == 137 || (exitCode != 0 && out.contains("MemoryError"))) {
            return String.format(
                    "OUTPUT_START\n❌ Memory Limit Exceeded\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                    timeMs, CONTAINER_MEMORY_KB);
        }
        if (exitCode != 0) {
            return String.format(
                    "OUTPUT_START\n❌ Runtime Error:\n%s\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                    out, timeMs, memKb);
        }
        return String.format(
                "OUTPUT_START\n%s\nOUTPUT_END\nTIME:%dms\nMEMORY:%dKB",
                out, timeMs, memKb);
    }

    // File helpers

    private static long readPeakKb(File f) {
        try {
            List<String> lines = Files.readAllLines(f.toPath());
            for (int i = lines.size() - 1; i >= 0; i--) {
                String s = lines.get(i).trim();
                if (s.matches("\\d+")) return Long.parseLong(s);
            }
        } catch (Exception ignored) {}
        return -1;
    }

    private static long readLong(File f, long fallback) {
        try {
            return Long.parseLong(Files.readString(f.toPath()).trim());
        } catch (Exception e) {
            return fallback;
        }
    }

    private static String readOutput(File f) {
        try (InputStream in = Files.newInputStream(f.toPath())) {
            return new String(in.readNBytes(MAX_OUTPUT_CHARS), StandardCharsets.UTF_8).trim();
        } catch (IOException e) {
            return "";
        }
    }

    private static String shellQuote(String s) {
        return "'" + s.replace("'", "'\\''") + "'";
    }

    private static void killExisting(String name) {
        try {
            new ProcessBuilder("docker", "rm", "-f", name)
                    .start()
                    .waitFor(5, TimeUnit.SECONDS);
        } catch (Exception ignored) {}
    }
}