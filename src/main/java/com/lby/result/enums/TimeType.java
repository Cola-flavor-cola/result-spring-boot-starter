package com.lby.result.enums;

/**
 * 表示时间的单位，提供了毫秒、秒、分钟和小时四种时间单位的枚举常量
 * 每个时间单位都对应一个转换因子，用于表示该单位相对于毫秒的时间长度
 */
public enum TimeType {

    /**
     * 毫秒时间单位，1毫秒。
     */
    MILLISECONDS(1),

    /**
     * 秒时间单位，1秒等于1000毫秒。
     */
    SECONDS(1000),

    /**
     * 分钟时间单位，1分钟等于60000毫秒。
     */
    MINUTES(60000),

    /**
     * 小时时间单位，1小时等于3600000毫秒。
     */
    HOURS(3600000);

    /**
     * 用于某些计算的比例因子。
     * 该因子用于调整特定计算的结果以满足特定需求。
     */
    private final long factor;


    /**
     * 构造一个时间单位对象
     *
     * @param factor 该时间单位相对于毫秒的转换因子
     */
    TimeType(long factor) {
        this.factor = factor;
    }


    /**
     * 将给定的值转换为毫秒。
     *
     * @param value 要转换的值，具体的时间单位未在原代码中指定，需根据上下文理解。
     * @return 返回转换后的毫秒值。
     */
    public long toMilliseconds(int value) {
        // 将值乘以因子以转换为毫秒。原代码中未提供因子的定义，它决定了原始时间单位与毫秒之间的转换关系。
        return value * factor;
    }

}