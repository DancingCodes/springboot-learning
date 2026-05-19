package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.Result;
import com.example.springbootlearning.dto.TransferRequest;
import com.example.springbootlearning.entity.Account;
import com.example.springbootlearning.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public Result<List<Account>> list() {
        return Result.success(accountService.list());
    }

    @PostMapping("/transfer")
    public Result<Void> transfer(@Valid @RequestBody TransferRequest req) {
        accountService.transfer(req.getFromId(), req.getToId(), req.getAmount());
        return Result.success("转账成功", null);
    }
}
