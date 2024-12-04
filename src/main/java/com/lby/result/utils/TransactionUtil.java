package com.lby.result.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

/**
 * TransactionUtil类提供了一系列与事务处理相关的工具方法
 * 它们主要用于简化事务的管理，确保数据的一致性和完整性
 */
public class TransactionUtil {

    private static final Logger logger = LoggerFactory.getLogger(TransactionUtil.class);

    /**
     * 执行一个事务性的操作
     * 此方法简化了事务管理过程，仅需提供事务管理器和一个回调操作即可
     * 适用于不需指定特定读写策略的情况，默认采用当前事务管理器的设置
     *
     * @param transactionManager 事务管理器，用于控制事务的开始、提交和回滚
     * @param action             在事务中执行的操作，必须实现TransactionCallback接口
     * @param <T>                回调操作返回的类型
     * @return 执行回调操作的结果
     */
    public static <T> T transaction(PlatformTransactionManager transactionManager, TransactionCallback<T> action) {
        return transaction(transactionManager, action, null);
    }

    /**
     * 执行一个平台事务管理器管理的事务，并返回事务执行结果
     *
     * @param transactionManager 平台事务管理器，用于管理事务
     * @param action             事务回调接口，定义事务执行的具体操作
     * @param definition         事务定义，包括隔离级别、传播行为和超时设置
     * @return 事务执行结果，类型由调用者指定
     * @throws IllegalArgumentException 如果transactionManager或action为null，则抛出此异常
     * @throws RuntimeException         如果事务执行失败，则抛出此异常
     */
    public static <T> T transaction(PlatformTransactionManager transactionManager, TransactionCallback<T> action, TransactionDefinition definition) {
        // 检查transactionManager是否为null，如果为null则抛出IllegalArgumentException异常
        if (transactionManager == null) {
            throw new IllegalArgumentException("transactionManager不能为空");
        }
        // 检查action是否为null，如果为null则抛出IllegalArgumentException异常
        if (action == null) {
            throw new IllegalArgumentException("action不能为空");
        }
        // 创建TransactionTemplate实例，用于执行事务
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        // 如果definition不为null，则根据definition配置事务模板的隔离级别、传播行为和超时
        if (definition != null) {
            template.setIsolationLevel(definition.getIsolationLevel());
            template.setPropagationBehavior(definition.getPropagationBehavior());
            template.setTimeout(definition.getTimeout());
        }
        // 尝试执行事务
        try {
            // 记录事务开始日志
            logger.info("开始事务");
            // 执行事务并获取结果
            T result = template.execute(action);
            // 记录事务成功完成日志
            logger.info("事务成功完成");
            // 返回事务执行结果
            return result;
        } catch (Exception e) {
            // 记录事务执行失败日志
            logger.error("事务执行失败", e);
            // 抛出RuntimeException异常，指示事务执行失败
            throw new RuntimeException("事务执行失败", e);
        }
    }

    /**
     * 定义事务属性
     *
     * @param isolationLevel      隔离级别
     * @param propagationBehavior 传播行为
     * @param timeout             超时时间（秒）
     * @param readOnly            是否只读
     * @return TransactionDefinition 实例
     */
    public static TransactionDefinition defineTransaction(int isolationLevel, int propagationBehavior, int timeout, boolean readOnly) {
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(isolationLevel);
        definition.setPropagationBehavior(propagationBehavior);
        definition.setTimeout(timeout);
        definition.setReadOnly(readOnly);
        return definition;
    }
}