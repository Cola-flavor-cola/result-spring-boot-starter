package com.lby.result.strategy.impl;

import com.lby.result.strategy.PageSizeStrategy;
import com.lby.result.utils.ObjectSizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class CpuBasedPageSizeStrategy implements PageSizeStrategy {

    private static final long TOTAL_AVAILABLE_MEMORY = Runtime.getRuntime().maxMemory();
    private static final int AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();
    private static final Logger logger = LoggerFactory.getLogger(CpuBasedPageSizeStrategy.class);

    /**
     * 根据给定的参数计算合适的页面大小
     * 此方法旨在根据线程池大小、可用内存和数据源大小，智能地调整页面大小，以优化资源利用
     *
     * @param pageSize       请求的页面大小，必须大于0
     * @param threadPoolSize 线程池大小，必须大于0
     * @param maxMemory      最大可用内存，必须大于0
     * @param sources        数据源列表，不能为空
     * @return 计算出的合适页面大小，不超过请求的页面大小
     * @throws IllegalArgumentException 如果任何输入参数无效（即不大于0或数据源列表为空或null）
     */
    @Override
    public int calculatePageSize(int pageSize, int threadPoolSize, int maxMemory, List<?> sources) {
        // 输入参数验证
        if (pageSize <= 0 || threadPoolSize <= 0 || maxMemory <= 0 || sources == null || sources.isEmpty()) {
            throw new IllegalArgumentException("无效的输入参数");
        }

        // 计算每核可用内存
        long memoryPerCore = TOTAL_AVAILABLE_MEMORY / AVAILABLE_CORES;

        // 估计每个对象的平均大小
        long averageObjectSize = sources != null ? estimateAverageObjectSize(sources) : 1; // 假设平均对象大小为1字节

        // 计算每核可以处理的对象数量
        int objectsPerCore = (int) (memoryPerCore / averageObjectSize);

        // 计算合适的页面大小
        int calculatedPageSize = Math.max(objectsPerCore, 1);

        // 确保返回的页面大小在合理范围内
        if (calculatedPageSize > pageSize) {
            return pageSize;
        } else {
            return calculatedPageSize;
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
