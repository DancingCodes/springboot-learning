package com.example.springbootlearning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootlearning.entity.Account;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Update;
import org.apache.ibatis.annotations.Param;
import java.math.BigDecimal;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {

    @Update("UPDATE account SET balance = balance - #{amount} WHERE id = #{id} AND balance >= #{amount}")
    int deduct(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE account SET balance = balance + #{amount} WHERE id = #{id}")
    int addBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE account SET balance = #{amount} WHERE id = #{id}")
    int setBalance(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Update("UPDATE account SET balance = balance * (1 + #{dailyRate})")
    void addInterest(@Param("dailyRate") BigDecimal dailyRate);

}