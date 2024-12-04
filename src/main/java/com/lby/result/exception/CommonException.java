package com.lby.result.exception;


/**
 * CommonException 类：通用异常类
 * 继承自 RuntimeException，用于处理运行时的异常情况
 * 该类用于封装具体的异常信息，并提供给调用者进行处理
 */
public class CommonException extends RuntimeException {

    /**
     * 构造函数：初始化CommonException类的实例
     * 该构造函数接受一个字符串类型的参数message，用于描述异常的具体信息
     * 通过调用父类的构造函数，将异常信息message传递给父类进行处理
     *
     * @param message 异常信息，用于描述引发异常的具体原因
     */
    public CommonException(String message) {
        super(message);
    }

    public CommonException(String message, Throwable cause) {
        super(message, cause);
    }
}


