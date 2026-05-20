package com.example.springbootlearning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("transfer_log")
@Schema(description = "转账日志")
public class TransferLog {

    @TableId(type = IdType.AUTO)
    @Schema(description = "日志ID")
    private Long id;

    @Schema(description = "转出账户ID")
    private Long fromAccountId;

    @Schema(description = "转入账户ID")
    private Long toAccountId;

    @Schema(description = "转账金额")
    private BigDecimal amount;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
