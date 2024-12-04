package com.lby.result.strategy.impl;


import com.lby.result.dto.BannedUserInfo;
import com.lby.result.enums.BanTime;
import com.lby.result.strategy.BanStrategy;

public class YearBanStrategyImpl implements BanStrategy {
    @Override
    public long calculateBanTime(BannedUserInfo dto) {
        return BanTime.YEARS.getTimes() * dto.getBanTime();
    }
}