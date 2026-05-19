package com.example.springbootlearning.controller;

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
    public List<User> list() {
        return userService.list();
    }

    @GetMapping("/{id}")
    public User getById(@PathVariable Long id) {
        return userService.getById(id);
    }

    @PostMapping
    public String add(@Valid @RequestBody User user) {
        userService.add(user);
        return "新增成功，id=" + user.getId();
    }

    @PutMapping
    public String update(@Valid @RequestBody User user) {
        userService.update(user);
        return "修改成功";
    }

    @DeleteMapping("/{id}")
    public String delete(@PathVariable Long id) {
        userService.delete(id);
        return "删除成功";
    }
}
