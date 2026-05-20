package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.Result;
import com.example.springbootlearning.dto.TransferDTO;
import com.example.springbootlearning.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

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
        return Result.success("转账成功", null);
    }

    @Operation(summary = "调整余额")
    @PutMapping("/{id}/balance")
    public Result<Void> adjustBalance(@Parameter(description = "账户ID") @PathVariable Long id,
                                       @RequestBody Map<String, BigDecimal> body) {
        accountService.adjustBalance(id, body.get("amount"));
        return Result.success("调整成功", null);
    }
}
