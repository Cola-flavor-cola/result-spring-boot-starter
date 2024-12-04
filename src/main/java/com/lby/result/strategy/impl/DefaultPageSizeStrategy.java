package com.lby.result.strategy.impl;

import com.lby.result.strategy.PageSizeStrategy;
import java.util.List;

public class DefaultPageSizeStrategy implements PageSizeStrategy {

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
        return pageSize;
    }

}