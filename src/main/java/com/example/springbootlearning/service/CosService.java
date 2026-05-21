package com.example.springbootlearning.service;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.region.Region;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.UUID;

@Slf4j
@Service
public class CosService {

    @Value("${cos.secret-id}")
    private String secretId;

    @Value("${cos.secret-key}")
    private String secretKey;

    @Value("${cos.region}")
    private String region;

    @Value("${cos.bucket}")
    private String bucket;

    @Value("${cos.cdn-domain}")
    private String cdnDomain;

    private COSClient client;

    @PostConstruct
    public void init() {
        COSCredentials cred = new BasicCOSCredentials(secretId, secretKey);
        ClientConfig config = new ClientConfig(new Region(region));
        client = new COSClient(cred, config);
        log.info("COS 客户端初始化完成: region={}, bucket={}", region, bucket);
    }

    @PreDestroy
    public void destroy() {
        if (client != null) {
            client.shutdown();
        }
    }

    public String upload(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String key = UUID.randomUUID().toString() + "_" + (originalName != null ? originalName : "");
        try (InputStream in = file.getInputStream()) {
            ObjectMetadata metadata = new ObjectMetadata();
            String contentType = file.getContentType();
            if (contentType != null) {
                metadata.setContentType(contentType);
            }
            PutObjectRequest request = new PutObjectRequest(bucket, key, in, metadata);
            client.putObject(request);
            log.info("文件上传到 COS: key={}, size={}", key, file.getSize());
            return key;
        } catch (Exception e) {
            throw new RuntimeException("上传文件到 COS 失败", e);
        }
    }

    public void delete(String key) {
        try {
            client.deleteObject(bucket, key);
            log.info("从 COS 删除文件: key={}", key);
        } catch (Exception e) {
            log.error("从 COS 删除文件失败: key={}", key, e);
        }
    }

    public InputStream getObject(String key) {
        COSObject object = client.getObject(bucket, key);
        return object.getObjectContent();
    }

    public String getCdnUrl(String key) {
        if (cdnDomain.endsWith("/")) {
            return cdnDomain + key;
        }
        return cdnDomain + "/" + key;
    }
}
