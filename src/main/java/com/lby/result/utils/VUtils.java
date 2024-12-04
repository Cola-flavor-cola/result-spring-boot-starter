package com.lby.result.utils;


import com.lby.result.exception.CommonException;
import com.lby.result.function.BranchFunction;
import com.lby.result.function.ThrowExceptionFunction;

/**
 * VUtils类提供了一些用于条件判断的工具方法
 */
public class VUtils {
    /**
     * 返回一个函数，该函数根据布尔值判断是否抛出异常
     *
     * @param b 布尔值，用于判断是否抛出异常
     * @return 返回一个ThrowExceptionFunction对象，调用该对象的apply方法时，如果b为true，则抛出异常
     */
    public static ThrowExceptionFunction isTure(boolean b){
        // 返回一个lambda表达式，该表达式在b为true时抛出CommonException异常
        return (errorMessage) -> {
            if (b){
                throw new CommonException(errorMessage);
            }
        };
    }

    /**
     * 返回一个分支函数，根据布尔值选择执行不同的Runnable
     *
     * @param b 布尔值，用于判断执行哪个分支
     * @return 返回一个BranchFunction对象，调用该对象的apply方法时，根据b的值执行相应的Runnable
     */
    public static BranchFunction isTureOrFalse(boolean b){
        // 返回一个lambda表达式，根据b的值执行相应的Runnable
        return (trueHandle, falseHandle) -> {
            if (b){
                trueHandle.run();
            } else {
                falseHandle.run();
            }
        };
    }
}