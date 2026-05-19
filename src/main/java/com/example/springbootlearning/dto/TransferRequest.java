package com.example.springbootlearning.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Schema(description = "转账请求")
public class TransferRequest {

    @NotNull(message = "转出账户ID不能为空")
    @Schema(description = "转出账户ID")
    private Long fromId;

    @NotNull(message = "转入账户ID不能为空")
    @Schema(description = "转入账户ID")
    private Long toId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    @Schema(description = "转账金额")
    private BigDecimal amount;
}
