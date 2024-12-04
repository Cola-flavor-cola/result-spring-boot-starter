package com.lby.result.utils;

import com.lby.result.enums.PageSizeStrategyType;
import com.lby.result.strategy.PageSizeStrategy;
import com.lby.result.strategy.impl.*;

import java.util.concurrent.ConcurrentHashMap;
/**
 * 策略映射类，用于存储和提供页面大小策略
 * 本类使用ConcurrentHashMap来存储不同类型的页面大小策略，
 * 以支持并发访问和策略的动态添加
 */
public class StrategyMap {
    // 存储页面大小策略的并发哈希映射
    private final ConcurrentHashMap<PageSizeStrategyType, PageSizeStrategy> strategies = new ConcurrentHashMap<>();

    /**
     * 构造方法，在创建StrategyMap实例时初始化策略映射
     * 默认向映射中添加几种预定义的页面大小策略，
     * 包括默认、动态、基于CPU、基于内存和混合策略
     */
    public StrategyMap() {
        strategies.put(PageSizeStrategyType.DEFAULT, new DefaultPageSizeStrategy());
        strategies.put(PageSizeStrategyType.DYNAMIC, new DynamicPageSizeStrategy());
        strategies.put(PageSizeStrategyType.CPUBASED, new CpuBasedPageSizeStrategy());
        strategies.put(PageSizeStrategyType.MEMORYBASED, new MemoryBasedPageSizeStrategy());
        strategies.put(PageSizeStrategyType.HYBRID, new HybridPageSizeStrategy());
    }

    /**
     * 根据策略类型获取对应的页面大小策略
     * 如果请求的策略类型不存在于映射中，则返回默认的页面大小策略
     *
     * @param strategyType 页面大小策略类型
     * @return 对应类型的页面大小策略，如果不存在则返回默认策略
     */
    public PageSizeStrategy get(PageSizeStrategyType strategyType) {
        return strategies.getOrDefault(strategyType, new DefaultPageSizeStrategy());
    }
}
