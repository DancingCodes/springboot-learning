package com.example.springbootlearning.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.entity.Account;
import com.example.springbootlearning.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;

    public List<Account> list() {
        return accountMapper.selectList(null);
    }

    public Page<Account> page(int pageNum, int pageSize) {
        return accountMapper.selectPage(new Page<>(pageNum, pageSize), null);
    }

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        int rows = accountMapper.deduct(fromId, amount);
        if (rows == 0) {
            throw new IllegalArgumentException("余额不足或账户不存在");
        }
        int rows2 = accountMapper.addBalance(toId, amount);
        if (rows2 == 0) {
            throw new IllegalArgumentException("目标账户不存在");
        }
    }
}
