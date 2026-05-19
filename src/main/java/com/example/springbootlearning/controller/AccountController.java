package com.example.springbootlearning.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.dto.Result;
import com.example.springbootlearning.dto.TransferRequest;
import com.example.springbootlearning.entity.Account;
import com.example.springbootlearning.service.AccountService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping
    public Result<Page<Account>> list(@RequestParam(defaultValue = "1") int page,
                                     @RequestParam(defaultValue = "10") int size) {
        return Result.success(accountService.page(page, size));
    }

    @PostMapping("/transfer")
    public Result<Void> transfer(@Valid @RequestBody TransferRequest req) {
        accountService.transfer(req.getFromId(), req.getToId(), req.getAmount());
        return Result.success("转账成功", null);
    }
}
