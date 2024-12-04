package com.lby.result.function;


/**
 * BranchFunction是一个函数式接口，用于处理二选一的分支逻辑
 * 它定义了一个方法trueOrFalseHandle，该方法接受两个Runnable参数，
 * 分别代表真和假的处理逻辑这个接口的目的是为了统一分支处理的调用方式，
 * 使得在需要进行简单分支判断时，能够有一个统一而简洁的处理方式
 */
@FunctionalInterface
public interface BranchFunction{

    /**
     * 根据条件执行不同的处理逻辑
     *
     * @param trueHandle 当条件为真时要执行的逻辑
     * @param falseHandle 当条件为假时要执行的逻辑
     */
    void trueOrFalseHandle(Runnable trueHandle, Runnable falseHandle);
}
