package com.lby.result.function;


/**
 * 标记为函数式接口，用于定义一个抛出异常的函数操作
 * 该接口提供了一个方法，用于指定如何抛出异常并传递错误信息
 */
@FunctionalInterface
public interface ThrowExceptionFunction {

    /**
     * 抛出带有指定错误信息的异常
     *
     * @param message 要包含在异常中的错误信息
     * 抛出异常是为了在函数式编程中更好地处理错误情况
     */
    void throwMessage(String message);
}
