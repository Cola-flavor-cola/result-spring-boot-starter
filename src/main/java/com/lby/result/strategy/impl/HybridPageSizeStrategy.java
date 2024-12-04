package com.lby.result.strategy.impl;

import com.lby.result.strategy.PageSizeStrategy;
import com.lby.result.utils.ObjectSizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.util.List;


/**
 * HybridPageSizeStrategy类实现了PageSizeStrategy接口，用于计算和返回页面大小
 * 它根据已知的总页面数量和期望的页面大小，调整实际的页面大小，以优化数据加载和内存使用
 */
public class HybridPageSizeStrategy implements PageSizeStrategy {

    private static final Logger logger = LoggerFactory.getLogger(HybridPageSizeStrategy.class);


    /**
     * 根据多种因素计算合适的页面大小
     * 此方法旨在根据页面大小、线程池大小、最大内存限制以及源数据列表的大小，
     * 动态调整和计算出最合适的页面大小，以优化数据处理和内存使用
     *
     * @param pageSize       页面大小，表示每个页面包含的记录数
     * @param threadPoolSize 线程池大小，处理数据的并发线程数
     * @param maxMemory      最大内存限制，单位为字节，对数据处理的内存上限
     * @param sources        数据源列表，需要进行分页处理的数据集合
     * @return 返回计算出的合适页面大小
     */
    @Override
    public int calculatePageSize(int pageSize, int threadPoolSize, int maxMemory, List<?> sources) {
        // 参数校验
        if (pageSize < 1 || threadPoolSize < 1 || maxMemory < 0 || sources == null) {
            throw new IllegalArgumentException("无效的输入参数");
        }

        if (sources.isEmpty()) {
            return 1; // 处理空列表的情况
        }

        try {
            int availableCores = Runtime.getRuntime().availableProcessors();
            MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
            MemoryUsage heapMemoryUsage = memoryMXBean.getHeapMemoryUsage();
            long totalAvailableMemory = heapMemoryUsage.getMax();
            long totalUsableMemory = heapMemoryUsage.getMax() - heapMemoryUsage.getUsed();
            long memoryPerCore = totalAvailableMemory / availableCores;
            long averageObjectSize = estimateAverageObjectSize(sources);
            int objectsPerCore = (int) (memoryPerCore / averageObjectSize);

            int objectsByMemory = (int) (totalUsableMemory / averageObjectSize);

            return Math.min(Math.max(objectsPerCore, 1), Math.max(objectsByMemory, 1));
        } catch (Exception e) {
            // 异常处理
            logger.error("计算页面大小错误: " + e.getMessage());
            return 1; // 返回默认值
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
