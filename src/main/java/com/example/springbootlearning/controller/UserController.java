package com.example.springbootlearning.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.example.springbootlearning.dto.Result;
import com.example.springbootlearning.dto.UserSaveDTO;
import com.example.springbootlearning.dto.UserVO;
import com.example.springbootlearning.entity.User;
import com.example.springbootlearning.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "用户管理")
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(summary = "分页查询用户")
    @GetMapping
    public Result<Page<UserVO>> list(@Parameter(description = "页码") @RequestParam(defaultValue = "1") int current,
                                   @Parameter(description = "每页条数") @RequestParam(defaultValue = "10") int size) {
        return Result.success(userService.page(current, size));
    }

    @Operation(summary = "查询单个用户")
    @GetMapping(params = "id")
    public Result<UserVO> getById(@Parameter(description = "用户ID") @RequestParam Long id) {
        return Result.success(userService.getById(id));
    }

    @Operation(summary = "新增用户")
    @PostMapping
    public Result<UserVO> add(@Valid @RequestBody UserSaveDTO request) {
        User user = toEntity(request);
        userService.add(user);
        return Result.success(userService.getById(user.getId()));
    }

    @Operation(summary = "修改用户")
    @PutMapping
    public Result<UserVO> update(@Valid @RequestBody UserSaveDTO request) {
        User user = toEntity(request);
        userService.update(user);
        return Result.success(userService.getById(request.getId()));
    }

    private User toEntity(UserSaveDTO dto) {
        User user = new User();
        user.setId(dto.getId());
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setAge(dto.getAge());
        user.setAvatar(dto.getAvatar());
        return user;
    }

    @Operation(summary = "删除用户")
    @DeleteMapping
    public Result<Void> delete(@Parameter(description = "用户ID") @RequestParam Long id) {
        userService.delete(id);
        return Result.success(null);
    }
}
