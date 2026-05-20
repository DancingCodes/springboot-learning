package com.example.springbootlearning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
@Schema(description = "用户新增/修改请求")
public class UserSaveDTO {

    @Schema(description = "用户ID（修改时必填）")
    private Long id;

    @NotBlank(message = "姓名不能为空")
    @Schema(description = "姓名")
    private String name;

    @Email(message = "邮箱格式不正确")
    @Schema(description = "邮箱")
    private String email;

    @Min(value = 0, message = "年龄不能为负数")
    @Max(value = 150, message = "年龄超出合理范围")
    @Schema(description = "年龄")
    private Integer age;

    @Schema(description = "头像文件名")
    private String avatar;
}
