package com.example.springbootlearning.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.entity.Account;
import com.example.springbootlearning.entity.User;
import com.example.springbootlearning.mapper.AccountMapper;
import com.example.springbootlearning.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;

    public List<User> list() {
        return userMapper.selectList(null);
    }

    public Page<User> page(int pageNum, int pageSize) {
        return userMapper.selectPage(new Page<>(pageNum, pageSize), null);
    }

    public User getById(Long id) { return userMapper.selectById(id); }

    @Transactional
    public void add(User user) {
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        Account account = new Account();
        account.setUserId(user.getId());
        account.setName(user.getName() + "的账户");
        account.setBalance(BigDecimal.ZERO);
        accountMapper.insert(account);
    }

    public void update(User user) {
        userMapper.updateById(user);
    }

    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}
