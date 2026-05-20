package com.example.springbootlearning.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.dto.UserVO;
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
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final AccountMapper accountMapper;

    public List<User> list() {
        return userMapper.selectList(null);
    }

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
                .map(u -> toVO(u, finalMap.get(u.getId())))
                .toList();
        Page<UserVO> result = new Page<>(pageNum, pageSize, page.getTotal());
        result.setRecords(voList);
        return result;
    }

    public UserVO getById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) return null;
        Account account = accountMapper.selectOne(
                new LambdaQueryWrapper<Account>().eq(Account::getUserId, id));
        return toVO(user, account);
    }

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

    private UserVO toVO(User user, Account account) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setName(user.getName());
        vo.setEmail(user.getEmail());
        vo.setAge(user.getAge());
        vo.setAvatar(user.getAvatar());
        vo.setCreateTime(user.getCreateTime());
        if (account != null) {
            vo.setAccountId(account.getId());
            vo.setBalance(account.getBalance());
        }
        return vo;
    }
}
