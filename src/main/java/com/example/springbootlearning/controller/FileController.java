package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.FileVO;
import com.example.springbootlearning.dto.Result;
import com.example.springbootlearning.service.CosService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Tag(name = "文件管理")
@RestController
@RequestMapping("/file")
@RequiredArgsConstructor
public class FileController {

    private final CosService cosService;

    @Operation(summary = "上传文件")
    @PostMapping("/upload")
    public Result<FileVO> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file) {

        String key = cosService.upload(file);

        FileVO vo = new FileVO();
        vo.setOriginalName(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        vo.setStoredName(key);
        vo.setSize(file.getSize());
        vo.setUrl(cosService.getCdnUrl(key));
        return Result.success(vo);
    }

    @Operation(summary = "下载文件")
    @GetMapping("/download")
    public ResponseEntity<Resource> download(
            @Parameter(description = "存储文件名") @RequestParam String storedName) {

        String originalName = storedName.contains("_")
                ? storedName.substring(storedName.indexOf("_") + 1)
                : storedName;

        Resource resource = new InputStreamResource(cosService.getObject(storedName));
        String disposition = "attachment; filename*=UTF-8''"
                + URLEncoder.encode(originalName, StandardCharsets.UTF_8);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition)
                .body(resource);
    }
}
