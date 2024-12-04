package com.lby.result.strategy.impl;


import com.lby.result.strategy.PageSizeStrategy;
import com.lby.result.utils.MemoryUtil;
import com.lby.result.utils.ObjectSizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class DynamicPageSizeStrategy implements PageSizeStrategy {
    private static final Logger logger = LoggerFactory.getLogger(DynamicPageSizeStrategy.class);

    /**
     * 根据给定的参数计算合适的页面大小
     * 此方法旨在根据线程池大小、最大内存限制以及数据源列表来动态计算最合适的页面大小
     *
     * @param pageSize 页面大小，这里似乎是一个占位参数，实际未使用
     * @param threadPoolSize 线程池大小，用于分担内存和处理能力
     * @param maxMemory 最大内存限制，用于控制内存使用
     * @param sources 数据源列表，用于估算数据对象的平均大小
     * @return 计算出的合适页面大小，至少为1
     * @throws IllegalArgumentException 如果线程池大小或最大内存限制小于等于0，或者数据源列表为空或null
     */
    @Override
    public int calculatePageSize(int pageSize, int threadPoolSize, int maxMemory, List<?> sources) {
        // 确保线程池大小的合法性
        if (threadPoolSize <= 0) {
            throw new IllegalArgumentException("线程池大小必须大于0");
        }
        // 确保最大内存限制的合法性
        if (maxMemory <= 0) {
            throw new IllegalArgumentException("最大内存限制必须大于0");
        }
        // 确保数据源列表的非空性
        if (sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("数据源列表不能为空");
        }

        try {
            // 每个线程可用的最大内存
            long memoryPerThread = maxMemory / threadPoolSize;
            // 数据源对象的平均大小
            long averageObjectSize = estimateAverageObjectSize(sources);
            // 确保数据源对象的平均大小大于0
            if (averageObjectSize <= 0) {
                throw new IllegalArgumentException("数据源对象的平均大小必须大于0");
            }
            // 每个线程可以处理的对象数量
            int objectsPerThread = (int) (memoryPerThread / averageObjectSize);
            // 负载因子
            double loadFactor = MemoryUtil.calculateLoadFactor();
            // 计算适应的页面大小
            int adaptivePageSize = (int) (objectsPerThread * loadFactor);
            // 确保返回的页面大小至少为1
            return Math.max(adaptivePageSize, 1);
        } catch (Exception e) {
            // 记录异常日志
            logger.error("计算页面大小时发生错误: " + e.getMessage());
            // 返回默认值或抛出异常
            return 1; // 或者 throw new RuntimeException(e);
        }
    }



    /**
     * 估算集合中对象的平均大小
     * 当集合为空时，返回默认大小1，以避免除以零的情况
     * 此方法不适用于精确科学，而是用于粗略估算内存使用情况
     *
     * @param sources 包含任意类型对象的集合
     * @return 集合中对象的平均大小如果集合为空，则返回1
     */
    private long estimateAverageObjectSize(List<?> sources) {
        int size = sources.size();
        if (size == 0) {
            return 1L;
        }
        long totalSize = 0;
        for (Object obj : sources) {
            try {
                totalSize += getObjectSize(obj);
            } catch (Exception e) {
                logger.error("获取对象大小错误: {}", e.getMessage(), e);
            }
        }
        return totalSize / size;
    }

    /**
     * 获取对象的大小
     * 本方法旨在提供一种简便的方式来估算对象在内存中的大小
     * 请注意，此方法返回的大小是估算值，实际大小可能会由于JVM的不同而有所差异
     *
     * @param obj 需要估算大小的对象
     * @return 对象的估算大小，以字节为单位
     */
    private long getObjectSize(Object obj) {
        return ObjectSizeUtil.estimateObjectSize(obj);
    }
}