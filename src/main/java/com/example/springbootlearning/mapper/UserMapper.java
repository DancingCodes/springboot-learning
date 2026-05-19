package com.example.springbootlearning.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.springbootlearning.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}