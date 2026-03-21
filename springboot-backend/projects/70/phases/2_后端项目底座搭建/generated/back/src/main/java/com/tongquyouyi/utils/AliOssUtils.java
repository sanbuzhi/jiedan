package com.tongquyouyi.utils;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.ObjectMetadata;
import com.tongquyouyi.config.AliOssProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

/**
 * 阿里云OSS工具类
 */
@Slf4j
@Component
public class AliOssUtils {

    @Autowired
    private AliOssProperties aliOssProperties;

    /**
     * 上传文件到阿里云OSS
     *
     * @param file 上传的文件
     * @return 文件访问URL
     */
    public String upload(MultipartFile file) {
        // 获取文件原始名称
        String originalFilename = file.getOriginalFilename();
        // 生成唯一文件名：UUID + 原始文件后缀
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString().replace("-", "") + suffix;

        // 创建OSS客户端
        OSS ossClient = new OSSClientBuilder().build(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret()
        );

        InputStream inputStream = null;
        try {
            inputStream = file.getInputStream();
            // 设置文件元数据
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());
            // 上传文件
            ossClient.putObject(aliOssProperties.getBucketName(), fileName, inputStream, metadata);
            // 拼接访问URL
            String url = "https://" + aliOssProperties.getBucketName() + "." + aliOssProperties.getEndpoint() + "/" + fileName;
            log.info("文件上传成功，URL：{}", url);
            return url;
        } catch (IOException e) {
            log.error("文件上传失败", e);
            throw new RuntimeException("文件上传失败", e);
        } finally {
            // 关闭流和OSS客户端
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e) {
                    log.error("关闭输入流失败", e);
                }
            }
            ossClient.shutdown();
        }
    }

    /**
     * 删除阿里云OSS上的文件
     *
     * @param url 文件访问URL
     */
    public void delete(String url) {
        // 从URL中提取文件名
        String fileName = url.substring(url.lastIndexOf("/") + 1);

        // 创建OSS客户端
        OSS ossClient = new OSSClientBuilder().build(
                aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret()
        );

        try {
            // 删除文件
            ossClient.deleteObject(aliOssProperties.getBucketName(), fileName);
            log.info("文件删除成功，文件名：{}", fileName);
        } catch (Exception e) {
            log.error("文件删除失败", e);
            throw new RuntimeException("文件删除失败", e);
        } finally {
            // 关闭OSS客户端
            ossClient.shutdown();
        }
    }
}