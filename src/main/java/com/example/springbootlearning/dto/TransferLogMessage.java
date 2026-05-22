package com.example.springbootlearning.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransferLogMessage implements Serializable {

    private Long fromAccountId;
    private Long toAccountId;
    private BigDecimal amount;
}
