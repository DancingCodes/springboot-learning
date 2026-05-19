package com.example.springbootlearning.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("account")
public class Account {

    @TableId(type = IdType.AUTO)
    private Long id;
    private String name;
    private BigDecimal balance;
}
