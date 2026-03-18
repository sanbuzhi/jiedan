package com.jiedan.service.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 依赖分析服务
 * 分析模块间的依赖关系，当接口变更时自动更新受影响的模块
 */
@Slf4j
@Service
public class DependencyAnalyzer {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String DEPENDENCY_CACHE_DIR = "./cache/dependencies/";

    // 依赖关系正则表达式模式
    private static final Pattern IMPORT_PATTERN = Pattern.compile(
        "import\\s+(?:static\\s+)?([\\w.]+)\\s*;"
    );
    private static final Pattern CLASS_REFERENCE_PATTERN = Pattern.compile(
        "\\b([A-Z][\\w]*)\\b"
    );
    private static final Pattern API_ENDPOINT_PATTERN = Pattern.compile(
        "@(GetMapping|PostMapping|PutMapping|DeleteMapping|RequestMapping)\\s*\\([^)]*\\)"
    );
    private static final Pattern FE_API_CALL_PATTERN = Pattern.compile(
        "(wx\\.request|uni\\.request|request)\\s*\\(\\s*\\{[^}]*url\\s*:\\s*['\"]([^'\"]+)['\"]"
    );

    @Data
    public static class DependencyGraph {
        private String projectId;
        private Map<String, ModuleNode> modules = new HashMap<>();
        private List<DependencyEdge> dependencies = new ArrayList<>();
        private long lastUpdated;
    }

    @Data
    public static class ModuleNode {
        private String moduleName;
        private String moduleType; // backend, frontend, database, config
        private List<String> files = new ArrayList<>();
        private List<String> exports = new ArrayList<>(); // 公开的类/接口/API
        private List<String> imports = new ArrayList<>(); // 依赖的其他模块
        private Map<String, ClassInfo> classInfos = new HashMap<>();
    }

    @Data
    public static class ClassInfo {
        private String className;
        private String packageName;
        private List<String> methods = new ArrayList<>();
        private List<String> fields = new ArrayList<>();
        private List<String> annotations = new ArrayList<>();
        private boolean isInterface;
        private boolean isPublic;
    }

    @Data
    public static class DependencyEdge {
        private String fromModule;
        private String toModule;
        private String dependencyType; // import, api_call, inheritance, composition
        private List<String> details = new ArrayList<>();
    }

    @Data
    public static class ImpactAnalysisResult {
        private String changedModule;
        private List<String> changedClasses;
        private List<ImpactedModule> impactedModules;
        private String analysisTime;
    }

    @Data
    public static class ImpactedModule {
        private String moduleName;
        private String impactType; // direct, indirect
        private List<String> impactedFiles;
        private List<String> requiredChanges;
        private int severity; // 1-5, 5 being highest
    }

    /**
     * 分析项目依赖关系
     */
    public DependencyGraph analyzeProject(String projectId, String projectPath) {
        log.info("开始分析项目依赖关系: {}", projectId);
        
        DependencyGraph graph = new DependencyGraph();
        graph.setProjectId(projectId);
        graph.setLastUpdated(System.currentTimeMillis());

        try {
            // 扫描项目结构
            scanProjectStructure(projectPath, graph);
            
            // 分析模块内部
            analyzeModuleInternals(graph);
            
            // 分析模块间依赖
            analyzeInterModuleDependencies(graph);
            
            // 保存依赖图
            saveDependencyGraph(graph);
            
            log.info("项目依赖分析完成: {} 个模块, {} 条依赖", 
                graph.getModules().size(), graph.getDependencies().size());
            
        } catch (Exception e) {
            log.error("依赖分析失败", e);
        }

        return graph;
    }

    /**
     * 扫描项目结构
     */
    private void scanProjectStructure(String projectPath, DependencyGraph graph) {
        Path path = Paths.get(projectPath);
        
        if (!Files.exists(path)) {
            log.warn("项目路径不存在: {}", projectPath);
            return;
        }

        // 识别模块
        Map<String, String> modulePaths = new HashMap<>();
        modulePaths.put("database", projectPath + "/database");
        modulePaths.put("backend", projectPath + "/springboot-backend");
        modulePaths.put("frontend", projectPath + "/miniprogram");
        modulePaths.put("config", projectPath + "/config");

        for (Map.Entry<String, String> entry : modulePaths.entrySet()) {
            String moduleName = entry.getKey();
            String modulePath = entry.getValue();
            
            File moduleDir = new File(modulePath);
            if (moduleDir.exists()) {
                ModuleNode node = new ModuleNode();
                node.setModuleName(moduleName);
                node.setModuleType(moduleName);
                
                // 收集模块内所有文件
                collectFiles(moduleDir, node.getFiles());
                
                graph.getModules().put(moduleName, node);
                log.debug("发现模块: {} ({} 个文件)", moduleName, node.getFiles().size());
            }
        }
    }

    /**
     * 收集目录中的所有代码文件
     */
    private void collectFiles(File dir, List<String> files) {
        File[] fileList = dir.listFiles();
        if (fileList == null) return;

        for (File file : fileList) {
            if (file.isDirectory()) {
                // 跳过常见非代码目录
                String name = file.getName();
                if (!name.equals("node_modules") && !name.equals("target") 
                    && !name.equals(".git") && !name.equals("cache")) {
                    collectFiles(file, files);
                }
            } else if (isCodeFile(file.getName())) {
                files.add(file.getAbsolutePath());
            }
        }
    }

    /**
     * 判断是否为代码文件
     */
    private boolean isCodeFile(String filename) {
        return filename.endsWith(".java") || filename.endsWith(".js") 
            || filename.endsWith(".ts") || filename.endsWith(".vue")
            || filename.endsWith(".sql") || filename.endsWith(".xml")
            || filename.endsWith(".yml") || filename.endsWith(".yaml")
            || filename.endsWith(".json");
    }

    /**
     * 分析模块内部结构
     */
    private void analyzeModuleInternals(DependencyGraph graph) {
        for (ModuleNode module : graph.getModules().values()) {
            for (String filePath : module.getFiles()) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(filePath)));
                    
                    if (filePath.endsWith(".java")) {
                        analyzeJavaFile(filePath, content, module);
                    } else if (filePath.endsWith(".js") || filePath.endsWith(".ts")) {
                        analyzeJsFile(filePath, content, module);
                    }
                } catch (IOException e) {
                    log.warn("无法读取文件: {}", filePath);
                }
            }
        }
    }

    /**
     * 分析Java文件
     */
    private void analyzeJavaFile(String filePath, String content, ModuleNode module) {
        ClassInfo classInfo = new ClassInfo();
        
        // 提取包名
        Pattern packagePattern = Pattern.compile("package\\s+([\\w.]+);");
        Matcher packageMatcher = packagePattern.matcher(content);
        if (packageMatcher.find()) {
            classInfo.setPackageName(packageMatcher.group(1));
        }

        // 提取类名
        Pattern classPattern = Pattern.compile(
            "(public\\s+)?(class|interface|enum)\\s+(\\w+)"
        );
        Matcher classMatcher = classPattern.matcher(content);
        if (classMatcher.find()) {
            classInfo.setClassName(classMatcher.group(3));
            classInfo.setPublic(classMatcher.group(1) != null);
            classInfo.setInterface("interface".equals(classMatcher.group(2)));
        }

        // 提取方法
        Pattern methodPattern = Pattern.compile(
            "(public|private|protected)\\s+[\\w<>\\[\\]]+\\s+(\\w+)\\s*\\("
        );
        Matcher methodMatcher = methodPattern.matcher(content);
        while (methodMatcher.find()) {
            classInfo.getMethods().add(methodMatcher.group(2));
        }

        // 提取字段
        Pattern fieldPattern = Pattern.compile(
            "(private|public|protected)\\s+[\\w<>\\[\\]]+\\s+(\\w+)\\s*[;=]"
        );
        Matcher fieldMatcher = fieldPattern.matcher(content);
        while (fieldMatcher.find()) {
            classInfo.getFields().add(fieldMatcher.group(2));
        }

        // 提取注解
        Pattern annotationPattern = Pattern.compile("@([\\w]+)");
        Matcher annotationMatcher = annotationPattern.matcher(content);
        while (annotationMatcher.find()) {
            classInfo.getAnnotations().add(annotationMatcher.group(1));
        }

        // 如果是公开的类/接口，添加到exports
        if (classInfo.isPublic()) {
            String fullName = classInfo.getPackageName() + "." + classInfo.getClassName();
            module.getExports().add(fullName);
        }

        if (classInfo.getClassName() != null) {
            module.getClassInfos().put(classInfo.getClassName(), classInfo);
        }
    }

    /**
     * 分析JS/TS文件
     */
    private void analyzeJsFile(String filePath, String content, ModuleNode module) {
        // 提取API调用
        Matcher apiMatcher = FE_API_CALL_PATTERN.matcher(content);
        while (apiMatcher.find()) {
            String url = apiMatcher.group(2);
            if (!module.getImports().contains(url)) {
                module.getImports().add(url);
            }
        }

        // 提取导出的函数/类
        Pattern exportPattern = Pattern.compile(
            "export\\s+(?:default\\s+)?(?:function|class|const|let|var)\\s+(\\w+)"
        );
        Matcher exportMatcher = exportPattern.matcher(content);
        while (exportMatcher.find()) {
            module.getExports().add(exportMatcher.group(1));
        }
    }

    /**
     * 分析模块间依赖
     */
    private void analyzeInterModuleDependencies(DependencyGraph graph) {
        ModuleNode backend = graph.getModules().get("backend");
        ModuleNode frontend = graph.getModules().get("frontend");
        ModuleNode database = graph.getModules().get("database");

        // 分析后端依赖数据库
        if (backend != null && database != null) {
            analyzeBackendDatabaseDependency(backend, database, graph);
        }

        // 分析前端依赖后端API
        if (frontend != null && backend != null) {
            analyzeFrontendBackendDependency(frontend, backend, graph);
        }
    }

    /**
     * 分析后端对数据库的依赖
     */
    private void analyzeBackendDatabaseDependency(ModuleNode backend, ModuleNode database, DependencyGraph graph) {
        DependencyEdge edge = new DependencyEdge();
        edge.setFromModule("backend");
        edge.setToModule("database");
        edge.setDependencyType("database_schema");

        // 分析后端中引用的表名
        for (String filePath : backend.getFiles()) {
            if (filePath.endsWith(".java")) {
                try {
                    String content = new String(Files.readAllBytes(Paths.get(filePath)));
                    
                    // 查找@Entity注解
                    if (content.contains("@Entity") || content.contains("@Table")) {
                        Pattern tablePattern = Pattern.compile("@Table\\s*\\(\\s*name\\s*=\\s*['\"]([^'\"]+)['\"]");
                        Matcher matcher = tablePattern.matcher(content);
                        while (matcher.find()) {
                            edge.getDetails().add(matcher.group(1));
                        }
                    }
                } catch (IOException e) {
                    log.warn("读取文件失败: {}", filePath);
                }
            }
        }

        if (!edge.getDetails().isEmpty()) {
            graph.getDependencies().add(edge);
        }
    }

    /**
     * 分析前端对后端的依赖
     */
    private void analyzeFrontendBackendDependency(ModuleNode frontend, ModuleNode backend, DependencyGraph graph) {
        // 提取后端API端点
        List<String> apiEndpoints = new ArrayList<>();
        for (ClassInfo classInfo : backend.getClassInfos().values()) {
            if (classInfo.getAnnotations().contains("RestController") 
                || classInfo.getAnnotations().contains("Controller")) {
                // 这个类是控制器，提取其API
                apiEndpoints.add(classInfo.getClassName());
            }
        }

        // 检查前端是否调用了这些API
        DependencyEdge edge = new DependencyEdge();
        edge.setFromModule("frontend");
        edge.setToModule("backend");
        edge.setDependencyType("api_call");

        for (String apiEndpoint : apiEndpoints) {
            edge.getDetails().add(apiEndpoint);
        }

        if (!edge.getDetails().isEmpty()) {
            graph.getDependencies().add(edge);
        }
    }

    /**
     * 分析变更影响
     */
    public ImpactAnalysisResult analyzeImpact(String projectId, String changedModule, 
                                               List<String> changedClasses) {
        log.info("分析变更影响: 模块={}, 变更类={}", changedModule, changedClasses);
        
        ImpactAnalysisResult result = new ImpactAnalysisResult();
        result.setChangedModule(changedModule);
        result.setChangedClasses(changedClasses);
        result.setAnalysisTime(new Date().toString());
        result.setImpactedModules(new ArrayList<>());

        // 加载依赖图
        DependencyGraph graph = loadDependencyGraph(projectId);
        if (graph == null) {
            log.warn("未找到依赖图: {}", projectId);
            return result;
        }

        // 查找直接受影响的模块
        for (DependencyEdge edge : graph.getDependencies()) {
            if (edge.getToModule().equals(changedModule)) {
                ImpactedModule impacted = new ImpactedModule();
                impacted.setModuleName(edge.getFromModule());
                impacted.setImpactType("direct");
                impacted.setImpactedFiles(new ArrayList<>());
                impacted.setRequiredChanges(new ArrayList<>());
                
                // 根据依赖类型确定需要哪些变更
                switch (edge.getDependencyType()) {
                    case "api_call":
                        impacted.getRequiredChanges().add("更新API调用");
                        impacted.getRequiredChanges().add("检查请求/响应格式");
                        impacted.setSeverity(4);
                        break;
                    case "database_schema":
                        impacted.getRequiredChanges().add("更新实体类");
                        impacted.getRequiredChanges().add("检查SQL查询");
                        impacted.setSeverity(5);
                        break;
                    case "import":
                        impacted.getRequiredChanges().add("更新导入语句");
                        impacted.getRequiredChanges().add("检查接口实现");
                        impacted.setSeverity(3);
                        break;
                    default:
                        impacted.getRequiredChanges().add("检查依赖兼容性");
                        impacted.setSeverity(2);
                }
                
                result.getImpactedModules().add(impacted);
            }
        }

        // 查找间接受影响的模块（依赖链）
        Set<String> processedModules = new HashSet<>();
        processedModules.add(changedModule);
        for (ImpactedModule direct : result.getImpactedModules()) {
            processedModules.add(direct.getModuleName());
        }

        for (ImpactedModule direct : new ArrayList<>(result.getImpactedModules())) {
            findIndirectImpacts(direct.getModuleName(), graph, result, processedModules, 1);
        }

        // 按严重程度排序
        result.getImpactedModules().sort((a, b) -> 
            Integer.compare(b.getSeverity(), a.getSeverity()));

        log.info("影响分析完成: {} 个受影响模块", result.getImpactedModules().size());
        return result;
    }

    /**
     * 查找间接受影响模块
     */
    private void findIndirectImpacts(String moduleName, DependencyGraph graph, 
                                      ImpactAnalysisResult result, 
                                      Set<String> processedModules, int depth) {
        if (depth > 3) return; // 限制递归深度

        for (DependencyEdge edge : graph.getDependencies()) {
            if (edge.getToModule().equals(moduleName) 
                && !processedModules.contains(edge.getFromModule())) {
                
                ImpactedModule impacted = new ImpactedModule();
                impacted.setModuleName(edge.getFromModule());
                impacted.setImpactType("indirect (level " + depth + ")");
                impacted.setImpactedFiles(new ArrayList<>());
                List<String> requiredChanges = new ArrayList<>();
                requiredChanges.add("检查间接依赖兼容性");
                impacted.setRequiredChanges(requiredChanges);
                impacted.setSeverity(Math.max(1, 3 - depth));
                
                result.getImpactedModules().add(impacted);
                processedModules.add(edge.getFromModule());
                
                // 递归查找
                findIndirectImpacts(edge.getFromModule(), graph, result, processedModules, depth + 1);
            }
        }
    }

    /**
     * 生成更新建议
     */
    public List<String> generateUpdateSuggestions(ImpactAnalysisResult impactResult) {
        List<String> suggestions = new ArrayList<>();
        
        suggestions.add("=== 变更影响分析报告 ===");
        suggestions.add(String.format("变更模块: %s", impactResult.getChangedModule()));
        suggestions.add(String.format("变更类: %s", String.join(", ", impactResult.getChangedClasses())));
        suggestions.add("");

        if (impactResult.getImpactedModules().isEmpty()) {
            suggestions.add("未发现受影响的其他模块");
        } else {
            suggestions.add(String.format("发现 %d 个受影响模块:", 
                impactResult.getImpactedModules().size()));
            suggestions.add("");

            for (ImpactedModule impacted : impactResult.getImpactedModules()) {
                suggestions.add(String.format("模块: %s", impacted.getModuleName()));
                suggestions.add(String.format("  影响类型: %s", impacted.getImpactType()));
                suggestions.add(String.format("  严重程度: %d/5", impacted.getSeverity()));
                suggestions.add("  需要更新:");
                for (String change : impacted.getRequiredChanges()) {
                    suggestions.add(String.format("    - %s", change));
                }
                suggestions.add("");
            }

            suggestions.add("=== 建议操作顺序 ===");
            suggestions.add("1. 优先处理严重程度高的模块");
            suggestions.add("2. 先更新直接依赖，再处理间接依赖");
            suggestions.add("3. 更新后运行测试验证兼容性");
        }

        return suggestions;
    }

    /**
     * 保存依赖图到文件
     */
    private void saveDependencyGraph(DependencyGraph graph) {
        try {
            File dir = new File(DEPENDENCY_CACHE_DIR);
            if (!dir.exists()) {
                dir.mkdirs();
            }

            File file = new File(dir, graph.getProjectId() + "_deps.json");
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, graph);
            log.debug("依赖图已保存: {}", file.getAbsolutePath());
        } catch (IOException e) {
            log.error("保存依赖图失败", e);
        }
    }

    /**
     * 从文件加载依赖图
     */
    public DependencyGraph loadDependencyGraph(String projectId) {
        try {
            File file = new File(DEPENDENCY_CACHE_DIR + projectId + "_deps.json");
            if (file.exists()) {
                return objectMapper.readValue(file, DependencyGraph.class);
            }
        } catch (IOException e) {
            log.error("加载依赖图失败", e);
        }
        return null;
    }

    /**
     * 获取模块的公开接口
     */
    public List<String> getModuleExports(String projectId, String moduleName) {
        DependencyGraph graph = loadDependencyGraph(projectId);
        if (graph != null && graph.getModules().containsKey(moduleName)) {
            return graph.getModules().get(moduleName).getExports();
        }
        return new ArrayList<>();
    }

    /**
     * 检查模块间兼容性
     */
    public boolean checkCompatibility(String projectId, String fromModule, String toModule) {
        DependencyGraph graph = loadDependencyGraph(projectId);
        if (graph == null) return true;

        // 查找依赖边
        for (DependencyEdge edge : graph.getDependencies()) {
            if (edge.getFromModule().equals(fromModule) && edge.getToModule().equals(toModule)) {
                // 检查依赖是否仍然有效
                // 这里可以添加更复杂的兼容性检查逻辑
                return !edge.getDetails().isEmpty();
            }
        }

        return true;
    }
}
