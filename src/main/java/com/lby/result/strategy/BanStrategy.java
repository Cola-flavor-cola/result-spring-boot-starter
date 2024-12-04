package com.lby.result.strategy;


import com.lby.result.dto.BannedUserInfo;

@FunctionalInterface
public interface BanStrategy {
    long calculateBanTime(BannedUserInfo dto);
}