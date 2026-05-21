package com.example.springbootlearning.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;
    private final CosService cosService;

    @Cacheable(value = "userPage", key = "#current + '_' + #size", unless = "#result == null")
    public Page<UserVO> page(int current, int size) {
        Page<UserVO> result = userMapper.selectUserPage(new Page<>(current, size));
        result.getRecords().forEach(this::fillAvatarUrl);
        return result;
    }

    @Cacheable(value = "user", key = "#id", unless = "#result == null")
    public UserVO getById(Long id) {
        UserVO vo = userMapper.selectUserWithAccount(id);
        if (vo != null) {
            fillAvatarUrl(vo);
        }
        return vo;
    }

    @CacheEvict(value = "userPage", allEntries = true)
    @Transactional
    public void add(User user) {
        user.setCreateTime(LocalDateTime.now());
        userMapper.insert(user);
        Account account = new Account();
        account.setUserId(user.getId());
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

    private void fillAvatarUrl(UserVO vo) {
        if (vo.getAvatar() != null && !vo.getAvatar().isEmpty()) {
            vo.setAvatarUrl(cosService.getCdnUrl(vo.getAvatar()));
        }
    }
}
