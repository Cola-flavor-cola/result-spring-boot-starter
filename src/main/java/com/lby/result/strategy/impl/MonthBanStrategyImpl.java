package com.lby.result.strategy.impl;


import com.lby.result.dto.BannedUserInfo;
import com.lby.result.enums.BanTime;
import com.lby.result.strategy.BanStrategy;

public class MonthBanStrategyImpl implements BanStrategy {
    @Override
    public long calculateBanTime(BannedUserInfo dto) {
        return BanTime.MONTH.getTimes() * dto.getBanTime();
    }
}