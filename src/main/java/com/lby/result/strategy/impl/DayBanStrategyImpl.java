package com.lby.result.strategy.impl;


import com.lby.result.dto.BannedUserInfo;
import com.lby.result.enums.BanTime;
import com.lby.result.strategy.BanStrategy;

public class DayBanStrategyImpl implements BanStrategy {
    @Override
    public long calculateBanTime(BannedUserInfo dto) {
        return BanTime.DAY.getTimes() * dto.getBanTime();
    }
}