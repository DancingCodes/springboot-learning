package com.example.springbootlearning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("account")
@Schema(description = "账户")
public class Account {

    @TableId(type = IdType.AUTO)
    @Schema(description = "账户ID")
    private Long id;

    @Schema(description = "账户名")
    private String name;

    @Schema(description = "余额")
    private BigDecimal balance;

    @Schema(description = "关联用户ID")
    private Long userId;
}
