package com.example.springbootlearning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "文件上传结果")
public class FileVO {
    @Schema(description = "原始文件名")
    private String originalName;
    @Schema(description = "存储文件名")
    private String storedName;
    @Schema(description = "文件大小（字节）")
    private long size;
}
