package com.example.springbootlearning.controller;

import com.example.springbootlearning.entity.Account;
import com.example.springbootlearning.mapper.AccountMapper;
import com.example.springbootlearning.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountMapper accountMapper;
    private final AccountService accountService;

    @GetMapping
    public List<Account> list() {
        return accountMapper.selectList(null);
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferRequest req) {
        accountService.transfer(req.getFromId(), req.getToId(), req.getAmount());
        return "转账成功";
    }
}
