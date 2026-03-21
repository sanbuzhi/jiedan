package com.kidswear.pos.controller;

import com.kidswear.pos.common.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 通用接口控制器
 */
@Slf4j
@RestController
@RequestMapping("/common")
@Tag(name = "通用接口", description = "上传、下载等通用功能")
public class CommonController {

    @Value("${file.upload.path}")
    private String uploadPath;

    @Value("${file.upload.url-prefix}")
    private String urlPrefix;

    @PostMapping("/upload")
    @Operation(summary = "上传文件")
    public Result<Map<String, String>> uploadFile(MultipartFile file) {
        if (file.isEmpty()) {
            return Result.error("上传文件不能为空");
        }

        // 获取文件扩展名
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null ? originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";

        // 生成文件名和路径
        String datePath = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        String fileName = UUID.randomUUID().toString().replace("-", "") + extension;
        String relativePath = datePath + "/" + fileName;
        String absolutePath = uploadPath + "/" + relativePath;

        // 创建目录
        File destFile = new File(absolutePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }

        try {
            // 保存文件
            file.transferTo(destFile);
            Map<String, String> result = new HashMap<>();
            result.put("url", urlPrefix + "/" + relativePath);
            result.put("fileName", fileName);
            return Result.success(result);
        } catch (IOException e) {
            log.error("文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }
}