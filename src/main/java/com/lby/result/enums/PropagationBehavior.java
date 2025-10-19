package com.lby.result.enums;

import org.springframework.transaction.TransactionDefinition;

/**
 * 事务传播行为枚举，定义事务方法如何与现有事务进行交互
 */
public enum PropagationBehavior {
    /**
     * 【默认策略】需要事务支持
     * - 如果当前存在事务，则加入该事务
     * - 如果当前没有事务，则新建一个事务
     *
     * 适用场景举例：
     * 用户注册操作（包含保存用户信息+初始化账户）
     * 当调用注册方法时：
     * - 如果外层没有事务 → 自动创建新事务
     * - 如果外层已有事务 → 共享同一个事务
     */
    REQUIRED(TransactionDefinition.PROPAGATION_REQUIRED, "需要事务（存在则加入，没有则新建）"),

    /**
     * 始终新建独立事务
     * - 挂起当前事务（如果存在）
     * - 创建全新独立事务
     * - 完成后恢复原事务
     *
     * 适用场景举例：
     * 操作日志记录需要与主事务分离
     * 即使主事务回滚，日志记录事务仍会提交
     */
    REQUIRES_NEW(TransactionDefinition.PROPAGATION_REQUIRES_NEW, "新建事务（独立运行）"),

    /**
     * 非事务方式执行
     * - 挂起当前事务（如果存在）
     * - 以无事务状态执行操作
     * - 完成后恢复原事务
     *
     * 适用场景举例：
     * 发送短信验证码等非关键操作
     * 即使外层事务回滚，短信仍然发送
     */
    NOT_SUPPORTED(TransactionDefinition.PROPAGATION_NOT_SUPPORTED, "非事务执行（挂起当前事务）"),

    /**
     * 强制要求存在事务
     * - 必须在已有事务上下文中调用
     * - 如果不存在事务 → 抛出异常
     *
     * 适用场景举例：
     * 资金账户变更等敏感操作
     * 防止被意外非事务方式调用
     */
    MANDATORY(TransactionDefinition.PROPAGATION_MANDATORY, "强制存在事务（否则抛异常）");

    private final int value;
    private final String description;

    PropagationBehavior(int value, String description) {
        this.value = value;
        this.description = description;
    }

    public int getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }
}