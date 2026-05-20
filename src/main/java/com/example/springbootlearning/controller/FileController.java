package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.FileVO;
import com.example.springbootlearning.dto.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
public class FileController {

    private final Path uploadDir;

    public FileController(@Value("${file.upload-dir:./uploads}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("创建上传目录失败", e);
        }
    }

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileVO> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file) throws IOException {

        String originalName = file.getOriginalFilename();
        String storedName = UUID.randomUUID().toString() + "_" + originalName;
        Path target = uploadDir.resolve(storedName);
        file.transferTo(target.toFile());

        FileVO vo = new FileVO();
        vo.setOriginalName(originalName != null ? originalName : "");
        vo.setStoredName(storedName);
        vo.setSize(file.getSize());
        return Result.success(vo);
    }

    @Operation(summary = "查看或下载文件")
    @GetMapping("/download")
    public ResponseEntity<Resource> download(
            @Parameter(description = "存储文件名") @RequestParam String storedName,
            @Parameter(description = "是否下载") @RequestParam(defaultValue = "false") boolean download) throws IOException {

        Path filePath = uploadDir.resolve(storedName);
        Resource resource = new FileSystemResource(filePath);

        if (!resource.exists()) {
            return ResponseEntity.notFound().build();
        }

        String originalName = storedName.contains("_")
                ? storedName.substring(storedName.indexOf("_") + 1)
                : storedName;

        String disposition = download
                ? "attachment; filename*=UTF-8''" + URLEncoder.encode(originalName, StandardCharsets.UTF_8)
                : "inline";

        MediaType mediaType = download ? MediaType.APPLICATION_OCTET_STREAM : MediaType.IMAGE_JPEG;

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }
}
