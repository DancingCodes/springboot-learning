package com.example.springbootlearning.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.entity.User;
import com.example.springbootlearning.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;

    public List<User> list() {
        return userMapper.selectList(null);
    }

    public Page<User> page(int pageNum, int pageSize) {
        return userMapper.selectPage(new Page<>(pageNum, pageSize), null);
    }

    public User getById(Long id) { return userMapper.selectById(id); }

    public void add(User user) {
        userMapper.insert(user);
    }

    public void update(User user) {
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}
