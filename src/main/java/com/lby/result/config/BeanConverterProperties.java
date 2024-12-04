package com.lby.result.config;

import com.lby.result.enums.PageSizeStrategyType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Bean转换器配置类
 * 该类用于配置Bean转换过程中的参数
 */
@Component
@ConfigurationProperties(prefix = "bean.converter")
public class BeanConverterProperties {

    /**
     * 阈值，用于确定何时启用特定的转换策略
     */
    private int threshold=1000;

    /**
     * 线程池大小，定义了执行转换任务的线程数量
     */
    private int threadPoolSize=10;

    /**
     * 是否启用线程转换
     */
    private boolean enableThreadConversion = false;

    /**
     * 每页的大小，用于分页处理大数据集
     */
    private int pageSize = 1000;

    /**
     * 最大内存，用于限制转换过程中的内存使用量(MB)
     */
    private int maxMemory = 10;
    /**
     * 分页策略类型
     * */
    private PageSizeStrategyType strategyType = PageSizeStrategyType.DEFAULT;

    /**
     * 获取阈值
     *
     * @return 当前配置的阈值
     */
    public int getThreshold() {
        return threshold;
    }

    /**
     * 设置阈值
     *
     * @param threshold 需要设置的阈值
     */
    public void setThreshold(int threshold) {
        this.threshold = threshold;
    }

    /**
     * 获取线程池大小
     *
     * @return 当前配置的线程池大小
     */
    public int getThreadPoolSize() {
        return threadPoolSize;
    }

    /**
     * 设置线程池大小
     *
     * @param threadPoolSize 需要设置的线程池大小
     */
    public void setThreadPoolSize(int threadPoolSize) {
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * 获取是否启用线程转换的标志
     *
     * @return 如果启用线程转换则返回true，否则返回false
     */
    public boolean isEnableThreadConversion() {
        return enableThreadConversion;
    }

    /**
     * 设置是否启用线程转换的标志
     *
     * @param enableThreadConversion 一个布尔值，用于启用或禁用线程转换
     */
    public void setEnableThreadConversion(boolean enableThreadConversion) {
        this.enableThreadConversion = enableThreadConversion;
    }

    /**
     * 获取每页的大小
     *
     * @return 每页的大小，以数量表示
     */
    public int getPageSize() {
        return pageSize;
    }

    /**
     * 设置每页的大小
     *
     * @param pageSize 每页的大小，以数量表示
     */
    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    /**
     * 获取最大内存值
     *
     * @return 最大内存值，以字节为单位
     */
    public int getMaxMemory() {
        return maxMemory;
    }

    /**
     * 设置最大内存值
     *
     * @param maxMemory 最大内存值，以字节为单位
     */
    public void setMaxMemory(int maxMemory) {
        this.maxMemory = maxMemory;
    }
    /**
     * 获取分页策略类型
     *
     * @return 分页策略类型
     */
    public PageSizeStrategyType getStrategyType() {
        return strategyType;
    }
    /**
     * 设置分页策略类型
     *
     * @param strategyType 分页策略类型
     */
    public void setStrategyType(PageSizeStrategyType strategyType) {
        this.strategyType = strategyType;
    }
}