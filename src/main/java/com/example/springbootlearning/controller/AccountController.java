package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.AdjustBalanceDTO;
import com.example.springbootlearning.dto.Result;
import com.example.springbootlearning.dto.TransferDTO;
import com.example.springbootlearning.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "账户管理")
@RestController
@RequestMapping("/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @Operation(summary = "转账")
    @PostMapping("/transfer")
    public Result<Void> transfer(@Valid @RequestBody TransferDTO req) {
        accountService.transfer(req.getFromId(), req.getToId(), req.getAmount());
        return Result.success(null);
    }

    @Operation(summary = "调整余额")
    @PutMapping("/balance")
    public Result<Void> adjustBalance(@Valid @RequestBody AdjustBalanceDTO req) {
        accountService.adjustBalance(req.getAccountId(), req.getAmount());
        return Result.success(null);
    }
}
