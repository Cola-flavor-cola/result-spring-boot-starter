package com.lby.result.utils;


import com.lby.result.context.BanContext;
import com.lby.result.dto.BannedUserInfo;
import com.lby.result.enums.BanTime;
import com.lby.result.exception.CommonException;
import com.lby.result.strategy.BanStrategy;
import com.lby.result.strategy.impl.DayBanStrategyImpl;
import com.lby.result.strategy.impl.MonthBanStrategyImpl;
import com.lby.result.strategy.impl.YearBanStrategyImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * StpUtils类是一个用于处理用户禁用逻辑的工具类
 */
@Slf4j
public class StpUtils {

    // 定义常量MSG，表示未知的封禁类型
    private final static String MSG = "未知的封禁类型";
    // 定义一个静态变量STRATEGY_MAP，用于存储不同封禁类型的策略
    private static final Map<BanTime, BanStrategy> STRATEGY_MAP = new HashMap<>();

    static {
        STRATEGY_MAP.put(BanTime.DAY, new DayBanStrategyImpl());
        STRATEGY_MAP.put(BanTime.MONTH, new MonthBanStrategyImpl());
        STRATEGY_MAP.put(BanTime.YEARS, new YearBanStrategyImpl());
    }

    /**
     * 根据禁止类型禁用用户
     *
     * @param bannedUserInfo 包含用户禁止信息的数据传输对象
     */
    public static void disable(BannedUserInfo bannedUserInfo) {
        // 根据禁止类型选择相应的禁止策略
        BanStrategy strategy = STRATEGY_MAP.get(bannedUserInfo.getType());
        if (strategy == null) {
            // 记录日志
            log.error("不支持的禁止类型: " + bannedUserInfo.getType());
            // 抛出带有详细信息的异常
            throw new CommonException("不支持的禁止类型: " + bannedUserInfo.getType());
        }

        // 创建禁止上下文并注入选择的禁止策略
        BanContext context = new BanContext(strategy);
        // 执行禁止用户操作
        context.disableUser(bannedUserInfo);
    }
}