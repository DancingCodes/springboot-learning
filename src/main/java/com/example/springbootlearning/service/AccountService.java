package com.example.springbootlearning.service;

import com.example.springbootlearning.config.RabbitMQConfig;
import com.example.springbootlearning.dto.TransferLogMessage;
import com.example.springbootlearning.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountMapper accountMapper;
    private final RabbitTemplate rabbitTemplate;

    @Transactional
    public void transfer(Long fromId, Long toId, BigDecimal amount) {
        int rows = accountMapper.deduct(fromId, amount);
        if (rows == 0) {
            throw new IllegalArgumentException("余额不足或账户不存在");
        }
        int rows2 = accountMapper.addBalance(toId, amount);
        if (rows2 == 0) {
            throw new IllegalArgumentException("目标账户不存在");
        }
        TransferLogMessage msg = new TransferLogMessage(fromId, toId, amount);
        rabbitTemplate.convertAndSend(RabbitMQConfig.EXCHANGE, RabbitMQConfig.ROUTING_KEY, msg);
    }

    public void adjustBalance(Long id, BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("余额不能为负数");
        }
        int rows = accountMapper.setBalance(id, amount);
        if (rows == 0) {
            throw new IllegalArgumentException("账户不存在");
        }
    }
}
