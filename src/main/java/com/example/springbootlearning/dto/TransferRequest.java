package com.example.springbootlearning.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {

    @NotNull(message = "转出账户ID不能为空")
    private Long fromId;

    @NotNull(message = "转入账户ID不能为空")
    private Long toId;

    @NotNull(message = "金额不能为空")
    @DecimalMin(value = "0.01", message = "金额必须大于0")
    private BigDecimal amount;
}
