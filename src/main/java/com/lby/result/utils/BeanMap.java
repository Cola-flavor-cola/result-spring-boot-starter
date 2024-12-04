package com.lby.result.utils;

import com.lby.result.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * 基于Spring BeanUtils的扩展，提供对象转换的工具方法。
 * 该类继承自BeanUtils，添加了针对单个对象和对象列表转换的功能。
 */
public class BeanMap extends BeanUtils {

    /**
     * 日志记录器。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanMap.class);


   /**
    * 将源对象转换为目标对象
    * 此方法使用泛型来提高代码的复用性，并允许在转换过程中进行自定义异常处理
    *
    * @param <S> 源对象的类型
    * @param <T> 目标对象的类型
    * @param source 源对象实例，不能为空
    * @param targetSupplier 一个Supplier函数式接口实例，用于创建目标对象，不能为空
    * @return 转换后的目标对象实例
    * @throws IllegalArgumentException 如果源对象或目标对象供应商为null，则抛出此异常
    * @throws CommonException 如果转换过程中发生异常，则抛出此自定义异常
    */
   public static <S, T> T convertTo(S source, Supplier<T> targetSupplier) {
       // 检查源对象和目标对象供应商是否为空，如果任一为空，则抛出非法参数异常
       if (source == null || targetSupplier == null) {
           throw new IllegalArgumentException("Source 和 targetSupplier 不能为空");
       }
       try {
           // 尝试将源对象转换为目标对象
           return convertTo(source, targetSupplier, null);
       } catch (Exception e) {
           // 如果转换过程中发生异常，记录日志或抛出自定义异常
           throw new CommonException("转换对象错误");
       }
   }




    /**
     * 将源对象转换为目标对象，并通过回调函数进行额外处理
     *
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @param source 源对象实例
     * @param targetSupplier 目标对象的供应者，用于创建目标对象实例
     * @param callBack 转换后的回调函数，用于执行额外的处理逻辑
     * @return 转换后的目标对象实例，如果转换失败则返回null
     */
    public static <S, T> T convertTo(S source, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
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
        } catch (Exception e) {
            // 记录异常信息
            LOGGER.error("复制属性错误: " + e.getMessage());
            return null;
        }

        // 如果回调函数不为空，则执行回调函数进行额外处理
        if (callBack != null) {
            try {
                // 执行回调函数
                callBack.callBack(source, target);
            } catch (Exception e) {
                // 记录异常信息
                LOGGER.error("回调错误: " + e.getMessage());
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
     * @param <S> 源对象的类型
     * @param <T> 目标对象的类型
     * @param sources 源对象列表，将被转换为目标对象
     * @param targetSupplier 目标对象的供应商，用于生成新的目标对象实例
     * @return 转换后的目标对象列表
     *
     * @throws IllegalArgumentException 如果源列表或目标供应商为 null，表示输入无效
     */
    public static <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier) {
        // 检查源列表是否为 null，如果是则抛出异常
        if (sources == null) {
            throw new CommonException("源列表不能为空");
        }
        // 检查目标供应商是否为 null，如果是则抛出异常
        if (targetSupplier == null) {
            throw new CommonException("目标不能为空");
        }
        // 调用重载方法，传入源列表和目标；转换器函数为 null
        return convertListTo(sources, targetSupplier, null);
    }



    /**
     * 将源列表转换为目标列表
     *
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * @param sources 源列表
     * @param targetSupplier 目标对象的供应者
     * @param callBack 转换过程中的回调接口，用于执行额外操作
     * @return 转换后的目标列表
     */
    public static <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack)  {
        // 检查输入参数是否为空
        if (sources == null || targetSupplier == null) {
            return Collections.emptyList(); // 返回空列表而不是 null
        }

        // 创建目标列表
        List<T> list = Collections.synchronizedList(new ArrayList<>(sources.size()));

        // 遍历源列表
        for (S source : sources) {
            try {
                // 创建目标对象实例
                T target = targetSupplier.get();
                // 复制属性从源对象到目标对象
                copyProperties(source, target);
                // 如果回调接口不为空，则执行回调方法
                if (callBack != null) {
                    callBack.callBack(source, target);
                }
                // 将目标对象添加到目标列表中
                list.add(target);
            } catch (Exception e) {
                // 记录异常信息
               LOGGER.error("错误转换元素: " + e.getMessage());
               break;
            }
        }
        // 返回目标列表
        return list;
    }

    /**
     * 转换回调接口，用于在对象转换过程中执行额外的逻辑。
     *
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * 回调接口
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     */
    @FunctionalInterface
    public interface ConvertCallBack<S, T> {
        /**
         * 执行转换回调。
         *
         * @param source 源对象
         * @param target 目标对象
         */
        void callBack(S t, T s);
    }

}
