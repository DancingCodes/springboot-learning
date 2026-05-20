package com.example.springbootlearning.service;

import com.example.springbootlearning.entity.TransferLog;
import com.example.springbootlearning.mapper.TransferLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class TransferLogService {

    private final TransferLogMapper transferLogMapper;

    @Async
    public void logAsync(Long fromAccountId, Long toAccountId, BigDecimal amount) {
        TransferLog log = new TransferLog();
        log.setFromAccountId(fromAccountId);
        log.setToAccountId(toAccountId);
        log.setAmount(amount);
        log.setCreateTime(LocalDateTime.now());
        transferLogMapper.insert(log);
    }
}
