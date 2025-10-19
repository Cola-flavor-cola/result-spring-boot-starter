package com.lby.result.enums;

import org.springframework.transaction.TransactionDefinition;

/**
 * 事务隔离级别枚举（控制事务间的可见性规则）
 *
 * <p>定义不同级别的事务隔离策略，平衡数据一致性与系统性能</p>
 */
public enum IsolationLevel {

    /**
     * 默认隔离级别
     * - 使用数据库默认隔离级别（通常为READ_COMMITTED）
     * - 不同数据库实现可能不同（MySQL默认REPEATABLE_READ）
     *
     * 典型应用场景：
     * 常规业务操作（在可接受数据库默认行为时使用）
     *
     * 注意事项：
     * 跨数据库系统时需特别注意行为差异
     */
    DEFAULT(TransactionDefinition.ISOLATION_DEFAULT, "默认级别（数据库默认设置）"),

    /**
     * 读未提交隔离级别
     * - 可能读取到其他事务未提交的数据（脏读）
     * - 可能遇到不可重复读、幻读
     *
     * 典型应用场景：
     * 实时数据监控（接受数据临时不一致）
     * 高并发统计类操作（需要最大读取性能）
     *
     * 风险提示：
     * 不适用于资金交易等需要数据准确性的场景
     */
    READ_UNCOMMITTED(TransactionDefinition.ISOLATION_READ_UNCOMMITTED, "读未提交（可能看到未提交数据）"),

    /**
     * 读已提交隔离级别
     * - 仅读取已提交的数据（避免脏读）
     * - 可能遇到不可重复读、幻读
     *
     * 典型应用场景：
     * 银行账户余额查询
     * 电商订单创建操作
     *
     * 并发现象示例：
     * 事务A多次读取同条数据可能得到不同结果
     */
    READ_COMMITTED(TransactionDefinition.ISOLATION_READ_COMMITTED, "读已提交（保证读取已提交数据）"),

    /**
     * 可重复读隔离级别
     * - 保证同一事务内多次读取结果一致
     * - 可能遇到幻读（新增数据可见）
     *
     * 典型应用场景：
     * 对账业务（需要数据快照一致性）
     * 复杂报表生成
     *
     * MySQL特别说明：
     * 通过MVCC机制避免幻读（非标准实现）
     */
    REPEATABLE_READ(TransactionDefinition.ISOLATION_REPEATABLE_READ, "可重复读（同一事务内结果一致）"),

    /**
     * 序列化隔离级别
     * - 完全事务隔离（串行执行）
     * - 避免所有并发问题（脏读、不可重复读、幻读）
     *
     * 典型应用场景：
     * 资金转账操作
     * 库存秒杀扣减
     *
     * 性能警告：
     * 高并发场景可能导致严重锁竞争
     */
    SERIALIZABLE(TransactionDefinition.ISOLATION_SERIALIZABLE, "序列化（完全隔离，性能最低）");

    private final int value;
    private final String description;

    IsolationLevel(int value, String description) {
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