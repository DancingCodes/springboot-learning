package com.example.springbootlearning.listener;

import com.example.springbootlearning.config.RabbitMQConfig;
import com.example.springbootlearning.dto.TransferLogMessage;
import com.example.springbootlearning.entity.TransferLog;
import com.example.springbootlearning.mapper.TransferLogMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TransferLogListener {

    private final TransferLogMapper transferLogMapper;

    @RabbitListener(queues = RabbitMQConfig.QUEUE)
    public void handleTransferLog(TransferLogMessage msg) {
        TransferLog log = new TransferLog();
        log.setFromAccountId(msg.getFromAccountId());
        log.setToAccountId(msg.getToAccountId());
        log.setAmount(msg.getAmount());
        log.setCreateTime(LocalDateTime.now());
        transferLogMapper.insert(log);
        log.info("转账日志写入成功: from={}, to={}, amount={}", msg.getFromAccountId(), msg.getToAccountId(), msg.getAmount());
    }
}
