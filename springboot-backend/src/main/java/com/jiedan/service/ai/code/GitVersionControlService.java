package com.jiedan.service.ai.code;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Git版本控制服务
 * 管理AI生成项目的版本控制
 */
@Slf4j
@Service
public class GitVersionControlService {

    private static final String PROJECTS_BASE_PATH = "projects";

    /**
     * 初始化项目Git仓库
     * 在AI明确需求接口调用完成后创建
     */
    public void initializeProjectRepository(String projectId, String projectName, String description) {
        String projectPath = PROJECTS_BASE_PATH + "/" + projectId;
        
        try {
            // 1. 创建项目目录
            Path path = Paths.get(projectPath);
            if (!Files.exists(path)) {
                Files.createDirectories(path);
                log.info("创建项目目录: {}", projectPath);
            }

            // 2. 初始化Git仓库
            Git git = Git.init()
                    .setDirectory(new File(projectPath))
                    .call();

            // 3. 创建初始README
            createInitialReadme(projectPath, projectName, description);

            // 4. 创建.gitignore
            createGitignore(projectPath);

            // 5. 初始提交
            git.add()
                    .addFilepattern(".")
                    .call();

            git.commit()
                    .setMessage("Initial commit: Project initialization\n\n" +
                            "Project: " + projectName + "\n" +
                            "Created: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                            "Description: " + (description != null ? description : "AI generated project"))
                    .call();

            log.info("Git仓库初始化完成, projectId: {}", projectId);

        } catch (Exception e) {
            log.error("Git仓库初始化失败, projectId: {}", projectId, e);
            throw new RuntimeException("Git仓库初始化失败", e);
        }
    }

    /**
     * 提交代码变更
     */
    public void commitChanges(String projectId, String message) {
        String projectPath = PROJECTS_BASE_PATH + "/" + projectId;
        
        try {
            Git git = Git.open(new File(projectPath));

            // 添加所有变更
            git.add()
                    .addFilepattern(".")
                    .call();

            // 提交
            git.commit()
                    .setMessage(message + "\n\n" +
                            "Timestamp: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")))
                    .call();

            log.info("代码提交完成, projectId: {}, message: {}", projectId, message);

        } catch (Exception e) {
            log.error("代码提交失败, projectId: {}", projectId, e);
            throw new RuntimeException("代码提交失败", e);
        }
    }

    /**
     * 创建分支
     */
    public void createBranch(String projectId, String branchName) {
        String projectPath = PROJECTS_BASE_PATH + "/" + projectId;
        
        try {
            Git git = Git.open(new File(projectPath));

            git.branchCreate()
                    .setName(branchName)
                    .call();

            log.info("分支创建完成, projectId: {}, branch: {}", projectId, branchName);

        } catch (Exception e) {
            log.error("分支创建失败, projectId: {}", projectId, e);
            throw new RuntimeException("分支创建失败", e);
        }
    }

    /**
     * 切换分支
     */
    public void checkoutBranch(String projectId, String branchName) {
        String projectPath = PROJECTS_BASE_PATH + "/" + projectId;
        
        try {
            Git git = Git.open(new File(projectPath));

            git.checkout()
                    .setName(branchName)
                    .call();

            log.info("分支切换完成, projectId: {}, branch: {}", projectId, branchName);

        } catch (Exception e) {
            log.error("分支切换失败, projectId: {}", projectId, e);
            throw new RuntimeException("分支切换失败", e);
        }
    }

    /**
     * 获取提交历史
     */
    public String getCommitHistory(String projectId, int maxCount) {
        String projectPath = PROJECTS_BASE_PATH + "/" + projectId;
        StringBuilder history = new StringBuilder();
        
        try {
            Git git = Git.open(new File(projectPath));

            Iterable<org.eclipse.jgit.revwalk.RevCommit> commits = git.log()
                    .setMaxCount(maxCount)
                    .call();

            for (org.eclipse.jgit.revwalk.RevCommit commit : commits) {
                history.append("Commit: ").append(commit.getName().substring(0, 7)).append("\n");
                history.append("Author: ").append(commit.getAuthorIdent().getName()).append("\n");
                history.append("Date: ").append(commit.getAuthorIdent().getWhen()).append("\n");
                history.append("Message: ").append(commit.getFullMessage()).append("\n");
                history.append("---\n");
            }

            return history.toString();

        } catch (Exception e) {
            log.error("获取提交历史失败, projectId: {}", projectId, e);
            return "获取提交历史失败: " + e.getMessage();
        }
    }

    /**
     * 回滚到指定版本
     */
    public void rollbackToCommit(String projectId, String commitId) {
        String projectPath = PROJECTS_BASE_PATH + "/" + projectId;
        
        try {
            Git git = Git.open(new File(projectPath));

            git.reset()
                    .setMode(org.eclipse.jgit.api.ResetCommand.ResetType.HARD)
                    .setRef(commitId)
                    .call();

            log.info("回滚完成, projectId: {}, commit: {}", projectId, commitId);

        } catch (Exception e) {
            log.error("回滚失败, projectId: {}", projectId, e);
            throw new RuntimeException("回滚失败", e);
        }
    }

    /**
     * 获取当前分支
     */
    public String getCurrentBranch(String projectId) {
        String projectPath = PROJECTS_BASE_PATH + "/" + projectId;
        
        try {
            Repository repository = FileRepositoryBuilder.create(new File(projectPath, ".git"));
            return repository.getBranch();

        } catch (Exception e) {
            log.error("获取当前分支失败, projectId: {}", projectId, e);
            return "unknown";
        }
    }

    /**
     * 查看文件差异
     */
    public String getDiff(String projectId, String oldCommit, String newCommit) {
        String projectPath = PROJECTS_BASE_PATH + "/" + projectId;
        
        try {
            Git git = Git.open(new File(projectPath));

            org.eclipse.jgit.diff.DiffFormatter diffFormatter = new org.eclipse.jgit.diff.DiffFormatter(
                    new java.io.ByteArrayOutputStream());
            diffFormatter.setRepository(git.getRepository());

            // 简化实现，实际应该解析diff输出
            return "Diff between " + oldCommit + " and " + newCommit + "\n" +
                   "(Detailed diff output would be here)";

        } catch (Exception e) {
            log.error("获取差异失败, projectId: {}", projectId, e);
            return "获取差异失败: " + e.getMessage();
        }
    }

    // ========== 私有方法 ==========

    private void createInitialReadme(String projectPath, String projectName, String description) throws IOException {
        String readmeContent = "# " + projectName + "\n\n" +
                (description != null ? description + "\n\n" : "") +
                "## 项目信息\n\n" +
                "- **项目名称**: " + projectName + "\n" +
                "- **创建时间**: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "\n" +
                "- **生成方式**: AI自动生成\n\n" +
                "## 版本控制\n\n" +
                "本项目使用Git进行版本控制，每次代码生成都会自动提交。\n\n" +
                "## 提交历史\n\n" +
                "查看提交历史：\n" +
                "```bash\n" +
                "git log\n" +
                "```\n";

        Path readmePath = Paths.get(projectPath, "README.md");
        Files.writeString(readmePath, readmeContent);
    }

    private void createGitignore(String projectPath) throws IOException {
        String gitignoreContent = "# Compiled class files\n" +
                "*.class\n\n" +
                "# Log files\n" +
                "*.log\n\n" +
                "# Package files\n" +
                "*.jar\n" +
                "*.war\n" +
                "*.nar\n" +
                "*.ear\n" +
                "*.zip\n" +
                "*.tar.gz\n" +
                "*.rar\n\n" +
                "# IDE files\n" +
                ".idea/\n" +
                "*.iml\n" +
                ".vscode/\n" +
                ".classpath\n" +
                ".project\n" +
                ".settings/\n\n" +
                "# Build directories\n" +
                "target/\n" +
                "build/\n" +
                "dist/\n" +
                "node_modules/\n\n" +
                "# OS files\n" +
                ".DS_Store\n" +
                "Thumbs.db\n\n" +
                "# AI generation logs\n" +
                "GENERATION_WARNING.txt\n" +
                "ai-generation-logs/\n";

        Path gitignorePath = Paths.get(projectPath, ".gitignore");
        Files.writeString(gitignorePath, gitignoreContent);
    }
}
