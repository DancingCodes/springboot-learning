package com.example.springbootlearning.config;

import com.example.springbootlearning.entity.SysUser;
import com.example.springbootlearning.mapper.SysUserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserMapper sysUserMapper;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (sysUserMapper.selectCount(null) == 0) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            sysUserMapper.insert(admin);
        }
    }
}
