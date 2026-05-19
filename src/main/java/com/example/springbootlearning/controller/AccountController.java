package com.example.springbootlearning.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.dto.Result;
import com.example.springbootlearning.dto.TransferRequest;
import com.example.springbootlearning.entity.Account;
import com.example.springbootlearning.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "分页查询账户")
    @GetMapping
    public Result<Page<Account>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") int page,
                                     @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        return Result.success(accountService.page(page, size));
    }

    @Operation(summary = "转账")
    @PostMapping("/transfer")
    public Result<Void> transfer(@Valid @RequestBody TransferRequest req) {
        accountService.transfer(req.getFromId(), req.getToId(), req.getAmount());
        return Result.success("转账成功", null);
    }
}
