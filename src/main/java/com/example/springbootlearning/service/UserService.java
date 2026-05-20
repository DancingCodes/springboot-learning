package com.example.springbootlearning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.converter.UserConverter;
import com.example.springbootlearning.dto.UserVO;
import com.example.springbootlearning.entity.Account;
import com.example.springbootlearning.entity.User;
import com.example.springbootlearning.mapper.AccountMapper;
import com.example.springbootlearning.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final UserConverter userConverter;

    public List<User> list() {
        return userMapper.selectList(null);
    }

    @Cacheable(value = "userPage", key = "#pageNum + '_' + #pageSize", unless = "#result == null")
    public Page<UserVO> page(int pageNum, int pageSize) {
        Page<User> page = userMapper.selectPage(new Page<>(pageNum, pageSize), null);
        List<Long> userIds = page.getRecords().stream().map(User::getId).toList();
        Map<Long, Account> accountMap = Map.of();
        if (!userIds.isEmpty()) {
            accountMap = accountMapper.selectList(
                    new LambdaQueryWrapper<Account>().in(Account::getUserId, userIds))
                    .stream()
                    .collect(Collectors.toMap(Account::getUserId, a -> a, (a, b) -> a));
        }
        Map<Long, Account> finalMap = accountMap;
        List<UserVO> voList = page.getRecords().stream()
                .map(u -> userConverter.toVO(u, finalMap.get(u.getId())))
                .toList();
        Page<UserVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(voList);
        return result;
    }

    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return null;
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getUserId, id));
        return userConverter.toVO(user, account);
    }

    @CacheEvict(value = "userPage", allEntries = true)
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

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#user.id"),
            @CacheEvict(value = "userPage", allEntries = true)
    })
    public void update(User user) {
        userMapper.updateById(user);
    }

    @Caching(evict = {
            @CacheEvict(value = "user", key = "#id"),
            @CacheEvict(value = "userPage", allEntries = true)
    })
    public void delete(Long id) {
        userMapper.deleteById(id);
    }
}
