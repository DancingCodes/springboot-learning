package com.example.springbootlearning.controller;

import com.example.springbootlearning.dto.Result;
import com.example.springbootlearning.entity.User;
import com.example.springbootlearning.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public Result<List<User>> list() {
        return Result.success(userService.list());
    }

    @GetMapping("/{id}")
    public Result<User> getById(@PathVariable Long id) {
        return Result.success(userService.getById(id));
    }

    @PostMapping
    public Result<User> add(@Valid @RequestBody User user) {
        userService.add(user);
        return Result.success("新增成功", user);
    }

    @PutMapping
    public Result<User> update(@Valid @RequestBody User user) {
        userService.update(user);
        return Result.success("修改成功", user);
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        userService.delete(id);
        return Result.success("删除成功", null);
    }
}
