package com.jiedan.controller.ai;

import com.jiedan.dto.ApiResponse;
import com.jiedan.dto.ai.code.*;
import com.jiedan.entity.AiDevelopmentFile;
import com.jiedan.entity.AiDevelopmentPhase;
import com.jiedan.entity.AiDevelopmentProject;
import com.jiedan.entity.AiDevelopmentRound;
import com.jiedan.entity.Requirement;
import com.jiedan.repository.*;
import com.jiedan.service.ai.ProjectDevelopmentManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/v1/ai/code")
@RequiredArgsConstructor
public class ProjectDevelopmentController {

    private final ProjectDevelopmentManager projectManager;
    private final AiDevelopmentProjectRepository projectRepository;
    private final AiDevelopmentPhaseRepository phaseRepository;
    private final AiDevelopmentRoundRepository roundRepository;
    private final AiDevelopmentFileRepository fileRepository;
    private final RequirementRepository requirementRepository;

    private static final String PROJECTS_BASE_DIR = "projects";

    @PostMapping("/project/start")
    public ApiResponse<StartProjectResponse> startProject(@RequestBody StartProjectRequest request) {
        log.info("接收项目开发请求, requirementId: {}", request.getRequirementId());
        
        if (request.getRequirementId() == null) {
            return ApiResponse.error("requirementId不能为空");
        }
        
        Requirement requirement = requirementRepository.findById(request.getRequirementId()).orElse(null);
        if (requirement == null) {
            return ApiResponse.error("需求不存在: " + request.getRequirementId());
        }
        
        String taskDoc = requirement.getAiTaskDoc();
        if (taskDoc == null || taskDoc.isEmpty()) {
            return ApiResponse.error("该需求尚未生成任务书");
        }
        
        String projectId = request.getProjectId() != null ? request.getProjectId() : String.valueOf(requirement.getId());
        
        if (projectRepository.existsByProjectId(projectId)) {
            return ApiResponse.error("项目ID已存在: " + projectId);
        }
        
        String projectName = requirement.getProjectType();
        if (projectName == null || projectName.isEmpty()) {
            projectName = "项目" + projectId;
        }
        
        projectManager.startProjectAsync(projectId, projectName, taskDoc);
        
        return ApiResponse.success(StartProjectResponse.success(projectId));
    }

    @GetMapping("/project/{projectId}/status")
    public ApiResponse<ProjectStatusResponse> getProjectStatus(@PathVariable String projectId) {
        Optional<AiDevelopmentProject> projectOpt = projectRepository.findByProjectId(projectId);
        
        if (projectOpt.isEmpty()) {
            return ApiResponse.error("项目不存在: " + projectId);
        }
        
        AiDevelopmentProject project = projectOpt.get();
        List<AiDevelopmentPhase> phases = phaseRepository.findByProjectIdOrderByPhaseAsc(projectId);
        
        ProjectStatusResponse response = new ProjectStatusResponse();
        response.setProjectId(project.getProjectId());
        response.setProjectName(project.getProjectName());
        response.setStatus(project.getStatus());
        response.setCurrentPhase(project.getCurrentPhase());
        response.setTotalPhases(7);
        response.setProgress(project.getProgress());
        response.setTotalFiles(project.getTotalFiles());
        response.setStartedAt(project.getStartedAt());
        response.setUpdatedAt(project.getUpdatedAt());
        
        List<ProjectStatusResponse.PhaseStatusDto> phaseDtoList = new ArrayList<>();
        for (AiDevelopmentPhase phase : phases) {
            ProjectStatusResponse.PhaseStatusDto dto = new ProjectStatusResponse.PhaseStatusDto();
            dto.setPhase(phase.getPhase());
            dto.setName(phase.getPhaseName());
            dto.setStatus(phase.getStatus());
            dto.setTotalRounds(phase.getTotalRounds());
            dto.setTotalFiles(phase.getTotalFiles());
            dto.setStartTime(phase.getStartedAt());
            dto.setEndTime(phase.getCompletedAt());
            
            if (phase.getStartedAt() != null && phase.getCompletedAt() != null) {
                long minutes = Duration.between(phase.getStartedAt(), phase.getCompletedAt()).toMinutes();
                dto.setDuration(minutes + "m");
            }
            
            List<AiDevelopmentFile> recentFiles = fileRepository
                .findByProjectIdAndPhaseOrderByRoundNumberAsc(projectId, phase.getPhase());
            dto.setRecentFiles(recentFiles.stream()
                .map(AiDevelopmentFile::getFilePath)
                .limit(5)
                .collect(Collectors.toList()));
            
            phaseDtoList.add(dto);
        }
        
        response.setPhases(phaseDtoList);
        response.setTotalRounds(phases.stream().mapToInt(p -> p.getTotalRounds()).sum());
        
        return ApiResponse.success(response);
    }

    @GetMapping("/project/{projectId}/phase/{phase}/status")
    public ApiResponse<PhaseStatusResponse> getPhaseStatus(@PathVariable String projectId, @PathVariable Integer phase) {
        Optional<AiDevelopmentPhase> phaseOpt = phaseRepository.findByProjectIdAndPhase(projectId, phase);
        
        if (phaseOpt.isEmpty()) {
            return ApiResponse.error("阶段不存在: 阶段" + phase);
        }
        
        AiDevelopmentPhase phaseEntity = phaseOpt.get();
        List<AiDevelopmentFile> files = fileRepository.findByProjectIdAndPhaseOrderByRoundNumberAsc(projectId, phase);
        
        PhaseStatusResponse response = new PhaseStatusResponse();
        response.setProjectId(projectId);
        response.setPhase(phase);
        response.setPhaseName(phaseEntity.getPhaseName());
        response.setStatus(phaseEntity.getStatus());
        response.setSessionId(phaseEntity.getSessionId());
        response.setTotalRounds(phaseEntity.getTotalRounds());
        response.setTotalFiles(phaseEntity.getTotalFiles());
        response.setStartTime(phaseEntity.getStartedAt());
        response.setUpdatedAt(phaseEntity.getUpdatedAt());
        
        List<PhaseStatusResponse.GeneratedFileDto> fileDtos = files.stream()
            .map(f -> {
                PhaseStatusResponse.GeneratedFileDto dto = new PhaseStatusResponse.GeneratedFileDto();
                dto.setPath(f.getFilePath());
                dto.setSize(f.getFileSize());
                dto.setType(f.getFileType());
                dto.setRound(f.getRoundNumber());
                dto.setComplete(f.getIsComplete());
                return dto;
            })
            .collect(Collectors.toList());
        
        response.setGeneratedFiles(fileDtos);
        response.setProgress(calculatePhaseProgress(phaseEntity));
        
        return ApiResponse.success(response);
    }

    @GetMapping("/project/{projectId}/phase/{phase}/progress")
    public ApiResponse<PhaseProgressResponse> getPhaseProgress(@PathVariable String projectId, @PathVariable Integer phase) {
        try {
            Path progressPath = Paths.get(PROJECTS_BASE_DIR, projectId, "phases", 
                "phase_" + phase, "progress.md");
            
            String content = "# 暂无进度记录\n";
            LocalDateTime lastUpdated = LocalDateTime.now();
            
            if (Files.exists(progressPath)) {
                content = Files.readString(progressPath);
                lastUpdated = LocalDateTime.parse(
                    Files.getLastModifiedTime(progressPath).toString(),
                    DateTimeFormatter.ISO_LOCAL_DATE_TIME
                );
            }
            
            PhaseProgressResponse response = new PhaseProgressResponse();
            response.setProjectId(projectId);
            response.setPhase(phase);
            response.setContent(content);
            response.setLastUpdatedAt(lastUpdated);
            
            return ApiResponse.success(response);
            
        } catch (IOException e) {
            return ApiResponse.error("读取进度文档失败: " + e.getMessage());
        }
    }

    @GetMapping("/project/{projectId}/phase/{phase}/round/{round}")
    public ApiResponse<RoundDetailResponse> getRoundDetail(@PathVariable String projectId, 
            @PathVariable Integer phase, @PathVariable Integer round) {
        
        List<AiDevelopmentPhase> phases = phaseRepository.findByProjectIdOrderByPhaseAsc(projectId);
        Optional<AiDevelopmentPhase> phaseOpt = phases.stream()
            .filter(p -> p.getPhase().equals(phase))
            .findFirst();
        
        if (phaseOpt.isEmpty()) {
            return ApiResponse.error("阶段不存在: " + phase);
        }
        
        Optional<AiDevelopmentRound> roundOpt = roundRepository
            .findByPhaseIdAndRoundNumber(phaseOpt.get().getId(), round);
        
        if (roundOpt.isEmpty()) {
            return ApiResponse.error("轮次不存在: " + round);
        }
        
        AiDevelopmentRound roundEntity = roundOpt.get();
        List<AiDevelopmentFile> files = fileRepository
            .findByProjectIdAndPhaseAndRoundNumber(projectId, phase, round);
        
        RoundDetailResponse response = new RoundDetailResponse();
        response.setProjectId(projectId);
        response.setPhase(phase);
        response.setRound(round);
        response.setStatus(roundEntity.getStatus());
        response.setInputSummary(truncate(roundEntity.getInputPrompt(), 500));
        response.setOutputSummary("生成" + files.size() + "个文件，总计" + 
            files.stream().mapToLong(AiDevelopmentFile::getFileSize).sum() + "字节");
        response.setTokensUsed(roundEntity.getTokensUsed());
        response.setContinuationCount(roundEntity.getContinuation());
        response.setFinishReason(roundEntity.getFinishReason());
        response.setCompletedAt(roundEntity.getCompletedAt());
        
        if (roundEntity.getStartedAt() != null && roundEntity.getCompletedAt() != null) {
            long seconds = Duration.between(roundEntity.getStartedAt(), roundEntity.getCompletedAt()).getSeconds();
            response.setDuration(seconds / 60 + "m " + seconds % 60 + "s");
        }
        
        List<RoundDetailResponse.FileInfo> fileInfos = files.stream()
            .map(f -> {
                RoundDetailResponse.FileInfo info = new RoundDetailResponse.FileInfo();
                info.setPath(f.getFilePath());
                info.setSize(f.getFileSize());
                return info;
            })
            .collect(Collectors.toList());
        
        response.setFiles(fileInfos);
        
        return ApiResponse.success(response);
    }

    private Integer calculatePhaseProgress(AiDevelopmentPhase phase) {
        if (phase.getTotalFiles() == null || phase.getTotalFiles() == 0) {
            return 0;
        }
        return Math.min(100, phase.getTotalFiles() * 100 / 20);
    }

    private String truncate(String str, int maxLength) {
        if (str == null) return "";
        return str.length() <= maxLength ? str : str.substring(0, maxLength) + "...";
    }
}
