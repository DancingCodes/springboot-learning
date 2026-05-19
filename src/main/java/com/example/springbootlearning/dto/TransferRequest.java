package com.example.springbootlearning.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class TransferRequest {
    private Long fromId;
    private Long toId;
    private BigDecimal amount;
}
