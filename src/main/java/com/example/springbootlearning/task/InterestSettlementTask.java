package com.example.springbootlearning.task;

import com.example.springbootlearning.mapper.AccountMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Component
@RequiredArgsConstructor
public class InterestSettlementTask {

    private final AccountMapper accountMapper;

    @Value("${interest.annual-rate}")
    private BigDecimal annualRate;

    @Scheduled(cron = "0 0 0 * * ?")
    public void settleDailyInterest() {
        BigDecimal dailyRate = annualRate.divide(BigDecimal.valueOf(365), 10, RoundingMode.HALF_UP);
        Long count = accountMapper.selectCount(null);
        accountMapper.addInterest(dailyRate);
        log.info("利息结算完成 | 年利率: {}% | 日利率: {}% | 影响账户: {}",
                annualRate.multiply(BigDecimal.valueOf(100)),
                dailyRate.multiply(BigDecimal.valueOf(100)).setScale(6, RoundingMode.HALF_UP),
                count);
    }
}
