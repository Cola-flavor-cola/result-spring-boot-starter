package com.lby.result.enums;


/**
 * 页大小策略类型枚举类
 * 用于定义在不同场景下选择不同页大小的策略
 */
public enum PageSizeStrategyType {
    /**
     * 默认页大小策略
     */
    DEFAULT,

    /**
     * 动态调整页大小策略
     */
    DYNAMIC,

    /**
     * 基于CPU使用情况的页大小策略
     */
    CPUBASED,

    /**
     * 基于内存使用情况的页大小策略
     */
    MEMORYBASED,

    /**
     * 混合页大小策略
     */
    HYBRID;
}
