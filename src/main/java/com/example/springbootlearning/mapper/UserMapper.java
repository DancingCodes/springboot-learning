package com.example.springbootlearning.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.dto.UserVO;
import com.example.springbootlearning.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    @Select("SELECT u.id, u.name, u.email, u.age, u.avatar, u.create_time, a.id AS account_id, a.balance " +
            "FROM user u LEFT JOIN account a ON u.id = a.user_id ORDER BY u.id DESC")
    Page<UserVO> selectUserPage(Page<UserVO> page);

    @Select("SELECT u.id, u.name, u.email, u.age, u.avatar, u.create_time, a.id AS account_id, a.balance " +
            "FROM user u LEFT JOIN account a ON u.id = a.user_id WHERE u.id = #{id}")
    UserVO selectUserWithAccount(Long id);
}
