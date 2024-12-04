package com.lby.result.utils;

public class MemoryUtil {

    /**
     * 计算负载因子，表示实际使用的内存占最大可用内存的比例
     *
     * @return 负载因子，范围在0到1之间
     */
    public static double calculateLoadFactor() {
        Runtime runtime = Runtime.getRuntime();
        long totalMemory = runtime.totalMemory(); // 当前JVM分配的总内存量
        long freeMemory = runtime.freeMemory();  // 当前JVM空闲内存量
        long usedMemory = totalMemory - freeMemory; // 实际使用的内存量

        // 最大可用内存量
        long maxMemory = runtime.maxMemory();

        // 计算负载因子
        return (double) usedMemory / maxMemory;
    }
}
