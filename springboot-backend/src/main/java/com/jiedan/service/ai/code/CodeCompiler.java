package com.jiedan.service.ai.code;

import com.jiedan.dto.ai.code.CompilationError;
import com.jiedan.dto.ai.code.CompilationResult;
import com.jiedan.dto.ai.code.GeneratedFile;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 代码编译器
 * 负责编译验证生成的代码
 */
@Slf4j
@Service
public class CodeCompiler {

    /**
     * 编译脚手架项目
     */
    public CompilationResult compileScaffold(String projectPath, String projectType) {
        log.info("编译脚手架, projectPath: {}, projectType: {}", projectPath, projectType);

        try {
            return switch (projectType.toLowerCase()) {
                case "springboot", "springcloud" -> compileMavenProject(projectPath);
                case "python" -> compilePythonProject(projectPath);
                case "wechat-miniprogram", "douyin-miniprogram" -> compileMiniprogram(projectPath);
                default -> {
                    log.warn("未知的项目类型: {}", projectType);
                    yield createFailResult("未知的项目类型: " + projectType);
                }
            };
        } catch (Exception e) {
            log.error("编译脚手架失败", e);
            return createFailResult("编译异常: " + e.getMessage());
        }
    }

    /**
     * 编译任务代码
     */
    public CompilationResult compileTaskCode(String projectPath, String taskId, List<GeneratedFile> files, String projectType) {
        log.info("编译任务代码, projectPath: {}, taskId: {}", projectPath, taskId);

        try {
            // 1. 将代码写入临时目录
            String taskCodePath = projectPath + "/src/task-" + taskId;
            writeFiles(taskCodePath, files);

            // 2. 根据项目类型编译
            return switch (projectType.toLowerCase()) {
                case "springboot", "springcloud" -> compileJavaFiles(projectPath, taskCodePath);
                case "python" -> compilePythonFiles(taskCodePath);
                default -> createSuccessResult();
            };

        } catch (Exception e) {
            log.error("编译任务代码失败", e);
            return createFailResult("编译异常: " + e.getMessage());
        }
    }

    /**
     * 编译整个项目
     */
    public CompilationResult compileProject(String projectPath, String projectType) {
        log.info("编译整个项目, projectPath: {}, projectType: {}", projectPath, projectType);

        try {
            return switch (projectType.toLowerCase()) {
                case "springboot", "springcloud" -> compileMavenProject(projectPath);
                case "python" -> compilePythonProject(projectPath);
                default -> createSuccessResult();
            };
        } catch (Exception e) {
            log.error("编译项目失败", e);
            return createFailResult("编译异常: " + e.getMessage());
        }
    }

    /**
     * 编译项目代码（脚手架 + 生成的代码）
     * 用于一次性生成模式
     */
    public CompilationResult compileProjectCode(String projectPath, String projectType,
                                                 List<GeneratedFile> scaffoldFiles,
                                                 List<GeneratedFile> generatedFiles) {
        log.info("编译项目代码, projectPath: {}, projectType: {}", projectPath, projectType);

        try {
            // 1. 写入脚手架文件（如果还没有）
            if (scaffoldFiles != null && !scaffoldFiles.isEmpty()) {
                writeFiles(projectPath, scaffoldFiles);
            }

            // 2. 写入生成的代码文件
            if (generatedFiles != null && !generatedFiles.isEmpty()) {
                writeFiles(projectPath + "/src/main", generatedFiles);
            }

            // 3. 编译整个项目
            return compileProject(projectPath, projectType);

        } catch (Exception e) {
            log.error("编译项目代码失败", e);
            return createFailResult("编译异常: " + e.getMessage());
        }
    }

    /**
     * 编译Maven项目
     */
    private CompilationResult compileMavenProject(String projectPath) {
        log.info("编译Maven项目: {}", projectPath);

        Process process = null;
        try {
            ProcessBuilder pb = new ProcessBuilder("mvn", "clean", "compile", "-q");
            pb.directory(new File(projectPath));
            pb.redirectErrorStream(true);

            process = pb.start();
            String output = readProcessOutput(process);

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Maven编译成功");
                return createSuccessResult();
            } else {
                log.error("Maven编译失败, exitCode: {}", exitCode);
                List<CompilationError> errors = parseMavenErrors(output);
                return createFailResult(errors);
            }

        } catch (Exception e) {
            log.error("Maven编译异常", e);
            return createFailResult("Maven编译异常: " + e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                log.warn("Maven编译进程被强制终止");
            }
        }
    }

    /**
     * 编译Java文件
     */
    private CompilationResult compileJavaFiles(String projectPath, String sourcePath) {
        log.info("编译Java文件: {}", sourcePath);

        Process process = null;
        try {
            // 查找所有Java文件
            List<String> javaFiles = findJavaFiles(sourcePath);

            if (javaFiles.isEmpty()) {
                return createSuccessResult();
            }

            // 构建javac命令
            List<String> command = new ArrayList<>();
            command.add("javac");
            command.add("-d");
            command.add(projectPath + "/target/classes");
            command.addAll(javaFiles);

            ProcessBuilder pb = new ProcessBuilder(command);
            pb.directory(new File(projectPath));
            pb.redirectErrorStream(true);

            process = pb.start();
            String output = readProcessOutput(process);

            int exitCode = process.waitFor();

            if (exitCode == 0) {
                log.info("Java编译成功");
                return createSuccessResult();
            } else {
                log.error("Java编译失败, exitCode: {}", exitCode);
                List<CompilationError> errors = parseJavacErrors(output);
                return createFailResult(errors);
            }

        } catch (Exception e) {
            log.error("Java编译异常", e);
            return createFailResult("Java编译异常: " + e.getMessage());
        } finally {
            if (process != null && process.isAlive()) {
                process.destroyForcibly();
                log.warn("Java编译进程被强制终止");
            }
        }
    }

    /**
     * 编译Python项目
     */
    private CompilationResult compilePythonProject(String projectPath) {
        log.info("编译Python项目: {}", projectPath);

        try {
            // Python是解释型语言，主要检查语法
            List<String> pythonFiles = findPythonFiles(projectPath);

            for (String file : pythonFiles) {
                ProcessBuilder pb = new ProcessBuilder("python", "-m", "py_compile", file);
                pb.directory(new File(projectPath));
                pb.redirectErrorStream(true);

                Process process = pb.start();
                String output = readProcessOutput(process);

                int exitCode = process.waitFor();

                if (exitCode != 0) {
                    log.error("Python语法检查失败: {}", file);
                    return createFailResult("Python语法错误: " + output);
                }
            }

            log.info("Python语法检查通过");
            return createSuccessResult();

        } catch (Exception e) {
            log.error("Python编译异常", e);
            return createFailResult("Python编译异常: " + e.getMessage());
        }
    }

    /**
     * 编译Python文件
     */
    private CompilationResult compilePythonFiles(String sourcePath) {
        return compilePythonProject(sourcePath);
    }

    /**
     * 编译小程序
     */
    private CompilationResult compileMiniprogram(String projectPath) {
        log.info("编译小程序项目: {}", projectPath);

        // 小程序主要检查JSON配置和文件结构
        // 实际编译需要使用微信开发者工具或抖音开发者工具
        // 这里只做基础检查

        try {
            // 检查必需的配置文件
            Path appJsonPath = Paths.get(projectPath, "app.json");
            if (!Files.exists(appJsonPath)) {
                return createFailResult("缺少app.json配置文件");
            }

            // 检查app.js
            Path appJsPath = Paths.get(projectPath, "app.js");
            if (!Files.exists(appJsPath)) {
                return createFailResult("缺少app.js文件");
            }

            log.info("小程序基础检查通过");
            return createSuccessResult();

        } catch (Exception e) {
            log.error("小程序编译异常", e);
            return createFailResult("小程序编译异常: " + e.getMessage());
        }
    }

    /**
     * 将文件写入磁盘
     */
    private void writeFiles(String basePath, List<GeneratedFile> files) throws IOException {
        for (GeneratedFile file : files) {
            Path filePath = Paths.get(basePath, file.getPath());
            Files.createDirectories(filePath.getParent());
            Files.writeString(filePath, file.getContent());
        }
    }

    /**
     * 查找所有Java文件
     */
    private List<String> findJavaFiles(String path) throws IOException {
        List<String> javaFiles = new ArrayList<>();
        Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".java"))
                .forEach(p -> javaFiles.add(p.toString()));
        return javaFiles;
    }

    /**
     * 查找所有Python文件
     */
    private List<String> findPythonFiles(String path) throws IOException {
        List<String> pythonFiles = new ArrayList<>();
        Files.walk(Paths.get(path))
                .filter(Files::isRegularFile)
                .filter(p -> p.toString().endsWith(".py"))
                .forEach(p -> pythonFiles.add(p.toString()));
        return pythonFiles;
    }

    /**
     * 读取进程输出
     */
    private String readProcessOutput(Process process) throws IOException {
        StringBuilder output = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                output.append(line).append("\n");
            }
        }
        return output.toString();
    }

    /**
     * 解析Maven编译错误
     */
    private List<CompilationError> parseMavenErrors(String output) {
        List<CompilationError> errors = new ArrayList<>();

        // Maven错误格式: [ERROR] /path/to/file.java:[line,column] error: message
        Pattern pattern = Pattern.compile("\\[ERROR\\]\\s*(.+?):\\[(\\d+),\\d+\\]\\s*error:\\s*(.+)");
        Matcher matcher = pattern.matcher(output);

        while (matcher.find()) {
            CompilationError error = CompilationError.builder()
                    .filePath(matcher.group(1))
                    .lineNumber(Integer.parseInt(matcher.group(2)))
                    .message(matcher.group(3))
                    .severity("error")
                    .build();
            errors.add(error);
        }

        return errors;
    }

    /**
     * 解析Javac编译错误
     */
    private List<CompilationError> parseJavacErrors(String output) {
        List<CompilationError> errors = new ArrayList<>();

        // Javac错误格式: /path/to/file.java:line: error: message
        Pattern pattern = Pattern.compile("(.+?):(\\d+):\\s*error:\\s*(.+)");
        Matcher matcher = pattern.matcher(output);

        while (matcher.find()) {
            CompilationError error = CompilationError.builder()
                    .filePath(matcher.group(1))
                    .lineNumber(Integer.parseInt(matcher.group(2)))
                    .message(matcher.group(3))
                    .severity("error")
                    .build();
            errors.add(error);
        }

        return errors;
    }

    /**
     * 创建成功的编译结果
     */
    private CompilationResult createSuccessResult() {
        return CompilationResult.builder()
                .success(true)
                .output("编译成功")
                .errors(new ArrayList<>())
                .build();
    }

    /**
     * 创建失败的编译结果
     */
    private CompilationResult createFailResult(String message) {
        return CompilationResult.builder()
                .success(false)
                .output(message)
                .errors(new ArrayList<>())
                .build();
    }

    /**
     * 创建失败的编译结果（带错误列表）
     */
    private CompilationResult createFailResult(List<CompilationError> errors) {
        return CompilationResult.builder()
                .success(false)
                .output("编译失败，发现 " + errors.size() + " 个错误")
                .errors(errors)
                .build();
    }
}
