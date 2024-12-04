package com.lby.result.strategy.impl;

import com.lby.result.strategy.PageSizeStrategy;
import com.lby.result.utils.ObjectSizeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;

public class MemoryBasedPageSizeStrategy implements PageSizeStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MemoryBasedPageSizeStrategy.class);

    /**
     * 根据给定的参数计算合适的页面大小
     * 此方法旨在根据系统可用内存、线程池大小和源数据量动态调整页面大小
     *
     * @param pageSize 页面大小，用于批量处理数据，必须大于0
     * @param threadPoolSize 线程池大小，表示并发处理的能力，必须大于0
     * @param maxMemory 系统分配的最大内存，用于限制内存使用量，防止内存溢出，必须大于0
     * @param sources 数据源列表，包含待处理的数据项，不能为空
     * @return 计算得出的合适页面大小，至少为1
     * @throws IllegalArgumentException 如果输入参数无效（小于等于0或为null），抛出此异常
     * @throws ArithmeticException 如果计算过程中出现除以零的情况，抛出此异常
     */
    @Override
    public int calculatePageSize(int pageSize, int threadPoolSize, int maxMemory, List<?> sources) {
        // 输入验证
        if (pageSize <= 0 || threadPoolSize <= 0 || maxMemory <= 0 || sources == null) {
            throw new IllegalArgumentException("无效的输入参数");
        }

        try {
            // 计算当前可用的最大内存，不超过系统设定的最大内存限制
            long totalAvailableMemory = Math.min(Runtime.getRuntime().maxMemory(), maxMemory);
            // 获取当前空闲内存大小
            long freeMemory = Runtime.getRuntime().freeMemory();
            // 计算可用来存储对象的内存大小
            long usableMemory = totalAvailableMemory - freeMemory;

            // 计算平均对象大小
            long averageObjectSize = estimateAverageObjectSize(sources);

            // 防止除以零
            if (averageObjectSize <= 0) {
                throw new ArithmeticException("平均对象大小为零或负值");
            }

            // 根据可用内存和平均对象大小计算每页可以包含的对象数量
            int objectsPerPage = (int) (usableMemory / averageObjectSize);
            // 确保返回的页面大小至少为1
            return Math.max(objectsPerPage, 1);
        } catch (Exception e) {
            // 异常处理
            logger.error("计算页面大小错误: " + e.getMessage());
            // 如果计算过程中出现异常，返回默认页面大小1
            return 1;
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