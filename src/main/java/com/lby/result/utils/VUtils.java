package com.lby.result.utils;


import com.lby.result.exception.CommonException;
import com.lby.result.function.BranchFunction;
import com.lby.result.function.ThrowExceptionFunction;

/**
 * VUtils类提供了一些用于条件判断的工具方法
 */
public class VUtils {

    /**
     * 创建一个在条件为真时抛出异常的函数。
     * 此方法用于简化条件检查并抛出异常的过程，使其更加直观和易于阅读。
     *
     * @param b 布尔条件，当此条件为真时，会抛出异常。
     * @return ThrowExceptionFunction接口的实现，用于在条件为真时抛出指定的错误信息。
     *
     * <pre>{@code
     * // 正常使用示例：在条件为真时抛出异常
     * VUtils.isTrue(true).throwException("条件为真时抛出的异常信息");
     *
     * // 条件为假时不抛出异常
     * VUtils.isTrue(false).throwException("此消息不会被抛出");
     * }</pre>
     */
    public static ThrowExceptionFunction isTrue(boolean b) {
        return (errorMessage) -> {
            if (b) {
                throw new CommonException(errorMessage);
            }
        };
    }

    /**
     * 创建一个在条件为假时抛出异常的函数。
     * 此方法用于简化条件检查并抛出异常的过程，使其更加直观和易于阅读。
     *
     * @param b 布尔条件，当此条件为假时，会抛出异常。
     * @return ThrowExceptionFunction接口的实现，用于在条件为假时抛出指定的错误信息。
     *
     * <pre>{@code
     * // 正常使用示例：在条件为假时抛出异常
     * VUtils.isFalse(false).throwException("条件为假时抛出的异常信息");
     *
     * // 条件为真时不抛出异常
     * VUtils.isFalse(true).throwException("此消息不会被抛出");
     * }</pre>
     */
    public static ThrowExceptionFunction isFalse(boolean b) {
        return (errorMessage) -> {
            if (!b) {
                throw new CommonException(errorMessage);
            }
        };
    }

    /**
     * 返回一个分支函数，根据布尔值选择执行不同的Runnable。
     * 此方法用于简化条件判断并执行相应的Runnable，使其更加直观和易于阅读。
     *
     * @param b 布尔值，用于判断执行哪个分支。
     * @return 返回一个BranchFunction对象，调用该对象的apply方法时，根据b的值执行相应的Runnable。
     *
     * <pre>{@code
     * // 正常使用示例：根据布尔值执行不同的Runnable
     * VUtils.branchOnBoolean(true).apply(
     *     () -> System.out.println("条件为真时执行"),
     *     () -> System.out.println("条件为假时执行")
     * );
     *
     * // 条件为假时执行另一个Runnable
     * VUtils.branchOnBoolean(false).apply(
     *     () -> System.out.println("条件为真时执行"),
     *     () -> System.out.println("条件为假时执行")
     * );
     * }</pre>
     */
    public static BranchFunction branchOnBoolean(boolean b) {
        return (trueHandle, falseHandle) -> {
            try {
                Runnable selectedHandle = b ? trueHandle : falseHandle;
                selectedHandle.run();
            } catch (Exception e) {
                System.err.println("Exception occurred in Runnable: " + e.getMessage());
            }
        };
    }
}