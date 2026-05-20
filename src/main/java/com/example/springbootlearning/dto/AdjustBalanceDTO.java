package com.example.springbootlearning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Schema(description = "调整余额请求")
public class AdjustBalanceDTO {
    @Schema(description = "账户ID")
    private Long accountId;
    @DecimalMin(value = "0", message = "余额不能为负数")
    @Schema(description = "新余额")
    private BigDecimal amount;
}
