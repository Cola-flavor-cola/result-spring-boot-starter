package com.lby.result.utils;

import com.lby.result.config.BeanConverterProperties;
import com.lby.result.enums.PageSizeStrategyType;
import com.lby.result.exception.CommonException;
import com.lby.result.strategy.PageSizeStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * BeanTransformer类用于实现Bean对象之间的转换操作
 * 它实现AutoCloseable接口，以确保在使用完BeanTransformer后可以优雅地关闭资源
 */
@Component
public class BeanTransformer implements AutoCloseable {

    // 日志记录器
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanTransformer.class);

    // BeanConverterProperties对象
    private final BeanConverterProperties properties;

    // 线程池对象
    private final ExecutorService executor;

    // 缓存 BeanInfo 以提高性能
    private static final Map<Class<?>, BeanInfo> beanInfoCache = new ConcurrentHashMap<>();

    // 缓存属性映射以提高性能
    private static final Map<Class<?>, Map<String, Method>> propertyWriteMethodCache = new ConcurrentHashMap<>();

    // 缓存策略
    private final StrategyMap strategyMap;


    /**
     * 构造函数用于初始化BeanTransformer类
     * 该类负责使用ModelMapper转换对象，并使用线程池执行转换操作
     *
     * @param properties BeanConverterProperties对象，包含转换操作所需的配置属性，如线程池大小
     */
    @Autowired
    public BeanTransformer(BeanConverterProperties properties) {
        // 初始化类成员变量properties
        this.properties = properties;
        // 根据配置属性中的线程池大小创建一个固定大小的线程池
        this.executor = Executors.newFixedThreadPool(properties.getThreadPoolSize());
        this.strategyMap = new StrategyMap();
    }

    /**
     * 将源对象转换为目标对象
     *
     * @param <S>            源对象的类型
     * @param <T>            目标对象的类型
     * @param source         源对象实例，不能为空
     * @param targetSupplier 一个Supplier函数式接口实例，用于创建目标对象，不能为空
     * @return 转换后的目标对象实例
     * @throws IllegalArgumentException 如果源对象或目标对象供应商为null，则抛出此异常
     */
    public <S, T> T convertTo(S source, Supplier<T> targetSupplier) {
        return convertTo(source, targetSupplier, null);
    }

    /**
     * 将源对象转换为目标对象，并通过回调函数进行额外处理
     *
     * @param <S>            源对象类型
     * @param <T>            目标对象类型
     * @param source         源对象实例
     * @param targetSupplier 目标对象的供应者，用于创建目标对象实例
     * @param callBack       转换后的回调函数，用于执行额外的处理逻辑
     * @return 转换后的目标对象实例
     * @throws IllegalArgumentException 如果源对象或目标对象供应商为null，则抛出此异常
     * @throws CommonException          如果转换过程中发生异常，则抛出自定义异常
     */
    public <S, T> T convertTo(S source, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        // 检查输入参数是否为 null
        if (source == null || targetSupplier == null) {
            // 记录日志或抛出自定义异常
            LOGGER.error("源或目标为空");
            return null;
        }

        // 使用目标对象的供应者创建一个新的目标对象实例
        T target = targetSupplier.get();

        try {
            // 复制属性
            copyProperties(source, target);
        } catch (IntrospectionException | IllegalAccessException | InvocationTargetException e) {
            // 记录异常信息
            LOGGER.error("复制属性错误: " + e.getMessage(), e);
            return null;
        }

        // 如果回调函数不为空，则执行回调函数进行额外处理
        if (callBack != null) {
            try {
                // 执行回调函数
                callBack.callBack(source, target);
            } catch (Exception e) {
                // 记录异常信息
                LOGGER.error("回调错误: " + e.getMessage(), e);
            }
        } else {
            // 回调为空时记录日志
            LOGGER.info("回调为空，不执行其他处理");
        }

        // 返回转换后的目标对象实例
        return target;
    }

    /**
     * 将源对象列表转换为目标对象列表。
     * 该方法使用供应商来创建目标类型的实例，从而实现对象的转换。
     *
     * @param <S>            源对象的类型
     * @param <T>            目标对象的类型
     * @param sources        源对象列表，将被转换为目标对象
     * @param targetSupplier 目标对象的供应商，用于生成新的目标对象实例
     * @return 转换后的目标对象列表
     * @throws IllegalArgumentException 如果源列表或目标供应商为 null，表示输入无效
     */
    public <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier) {
        return convertListTo(sources, targetSupplier, null);
    }

    /**
     * 将源列表转换为目标列表
     *
     * @param <S>            源对象类型
     * @param <T>            目标对象类型
     * @param sources        源列表
     * @param targetSupplier 目标对象的供应者
     * @param callBack       转换过程中的回调接口，用于执行额外操作
     * @return 转换后的目标列表
     */
    public <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        if (sources == null) {
            throw new IllegalArgumentException("源列表不能为空");
        }
        if (targetSupplier == null) {
            throw new IllegalArgumentException("目标不能为空");
        }
        if (properties.isEnableThreadConversion() || sources.size() >= properties.getThreshold()) {
            LOGGER.info("当前策略:{}", properties.getStrategyType());
            LOGGER.info("启用多线程转换，阈值为: {}", properties.getThreshold());
            LOGGER.info("线程池大小为: {}", properties.getThreadPoolSize());
            LOGGER.info("分页大小为: {}", properties.getPageSize());
            LOGGER.info("源列表大小为: {}", sources.size());
            return convertListToAsync(sources, targetSupplier, callBack);
        } else {
            return convertListToSingleThreaded(sources, targetSupplier, callBack);
        }
    }


    /**
     * 单线程将源对象列表转换为目标对象列表
     *
     * @param <S>            源对象的类型
     * @param <T>            目标对象的类型
     * @param sources        源对象列表，将被转换为目标对象
     * @param targetSupplier 目标对象的供应商，用于生成新的目标对象实例
     * @param callBack       转换过程中的回调接口，用于执行额外操作
     * @return 转换后的目标对象列表
     */
    private <S, T> List<T> convertListToSingleThreaded(List<S> sources, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        return sources.stream().map(source -> {
            try {
                // 创建目标对象实例
                T target = targetSupplier.get();
                // 复制属性从源对象到目标对象
                copyProperties(source, target);
                // 如果回调接口不为空，则执行回调方法
                if (callBack != null) {
                    callBack.callBack(source, target);
                }
                // 返回目标对象
                return target;
            } catch (Exception e) {
                // 记录异常信息
                LOGGER.error("错误转换元素: {}", e.getMessage(), e);
                return null; // 或者可以选择抛出异常
            }
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }


    /**
     * 异步转换列表
     * 该方法通过异步方式将一个列表中的元素转换为另一个列表中的元素
     * 它使用了CompletableFuture来并行处理列表元素的转换，以提高性能
     *
     * @param sources 源列表，包含待转换的元素
     * @param targetSupplier 目标元素的构造函数
     * @param callBack 转换回调接口，用于定义如何将源元素转换为目标元素
     * @param <S> 源列表元素的类型
     * @param <T> 目标列表元素的类型
     * @return 转换后的目标列表
     * @throws CommonException 如果转换过程中发生异常
     */
    private <S, T> List<T> convertListToAsync(List<S> sources, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        // 根据配置的策略类型获取对应的分页策略
        PageSizeStrategy pageSizeStrategy = strategyMap.get(properties.getStrategyType());
        // 计算每次处理的列表大小
        int pageSize = pageSizeStrategy.calculatePageSize(properties.getPageSize(),
                properties.getThreadPoolSize(), properties.getMaxMemory(), sources);
        // 存储所有异步任务的CompletableFuture对象
        List<CompletableFuture<List<T>>> futures = new ArrayList<>();
        // 分批处理源列表元素，每批处理的大小为pageSize
        for (int i = 0; i < sources.size(); i += pageSize) {
            int end = Math.min(i + pageSize, sources.size());
            List<S> batch = sources.subList(i, end);
            // 异步处理当前批次的元素转换
            CompletableFuture<List<T>> future = CompletableFuture.supplyAsync(() -> convertListToSingleThreaded(batch, targetSupplier, callBack), executor);
            futures.add(future);
        }
        // 等待所有异步任务完成
        CompletableFuture<Void> allFutures = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));

        try {
            allFutures.join();
            // 使用并行流来进一步提高性能，合并所有异步任务的结果
            return futures.parallelStream()
                    .map(CompletableFuture::join)
                    .flatMap(List::stream)
                    .collect(Collectors.toCollection(ArrayList::new));
        } catch (Exception e) {
            LOGGER.error("转换过程中发生异常", e);
            throw new CommonException("转换过程中发生异常", e);
        } finally {
            executor.shutdown();
        }
    }


    /**
     * 复制属性从源对象到目标对象
     *
     * @param source 源对象
     * @param target 目标对象
     * @throws IntrospectionException 引发内省异常
     * @throws IllegalAccessException 引发非法访问异常
     * @throws InvocationTargetException 引发调用目标异常
     */
    private void copyProperties(Object source, Object target)
            throws IntrospectionException, IllegalAccessException, InvocationTargetException {
        Class<?> sourceClass = source.getClass();
        Class<?> targetClass = target.getClass();

        BeanInfo sourceBeanInfo = getBeanInfo(sourceClass);
        PropertyDescriptor[] sourcePropertyDescriptors = sourceBeanInfo.getPropertyDescriptors();

        Map<String, Method> targetWriteMethods = getPropertyWriteMethods(targetClass);

        for (PropertyDescriptor sourcePropertyDescriptor : sourcePropertyDescriptors) {
            String propertyName = sourcePropertyDescriptor.getName();
            Method readMethod = sourcePropertyDescriptor.getReadMethod();

            if (readMethod != null && targetWriteMethods.containsKey(propertyName)) {
                Method writeMethod = targetWriteMethods.get(propertyName);
                Object value = readMethod.invoke(source, (Object[]) null);
                writeMethod.invoke(target, value);
            }
        }
    }

    /**
     * 获取 BeanInfo 并缓存
     *
     * @param clazz 类
     * @return BeanInfo 实例
     * @throws IntrospectionException 引发内省异常
     */
    private BeanInfo getBeanInfo(Class<?> clazz) throws IntrospectionException {
        return beanInfoCache.computeIfAbsent(clazz, c -> {
            try {
                return Introspector.getBeanInfo(c);
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
        });
    }

    /**
     * 获取属性写方法映射并缓存
     *
     * @param clazz 类
     * @return 属性名到写方法的映射
     * @throws IntrospectionException 引发内省异常
     */
    private Map<String, Method> getPropertyWriteMethods(Class<?> clazz) throws IntrospectionException {
        return propertyWriteMethodCache.computeIfAbsent(clazz, c -> {
            Map<String, Method> methodMap = new HashMap<>();
            try {
                BeanInfo beanInfo = Introspector.getBeanInfo(c);
                PropertyDescriptor[] propertyDescriptors = beanInfo.getPropertyDescriptors();
                for (PropertyDescriptor pd : propertyDescriptors) {
                    String name = pd.getName();
                    Method writeMethod = pd.getWriteMethod();
                    if (writeMethod != null) {
                        methodMap.put(name, writeMethod);
                    }
                }
            } catch (IntrospectionException e) {
                throw new RuntimeException(e);
            }
            return methodMap;
        });
    }

    /**
     * 关闭线程池
     */
    @Override
    public void close() {
        executor.shutdown();
        try {
            if (!executor.awaitTermination(properties.getThreadPoolSize(), TimeUnit.SECONDS)) {
                executor.shutdownNow();
            }
        } catch (InterruptedException ie) {
            executor.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    /**
     * 回调接口，用于在转换过程中执行额外的操作
     */
    @FunctionalInterface
    public interface ConvertCallBack<S, T> {
        void callBack(S source, T target);
    }

}
