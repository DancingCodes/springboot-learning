package com.example.springbootlearning.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootlearning.entity.TransferLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TransferLogMapper extends BaseMapper<TransferLog> {
}
