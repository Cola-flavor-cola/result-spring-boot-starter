package com.lby.result.utils;

import com.lby.result.enums.IsolationLevel;
import com.lby.result.enums.PropagationBehavior;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * 事务管理工具类，提供事务执行和配置的封装方法。<pre>{@code
 * // 简单事务执行示例（使用默认配置）
 * TransactionUtil.transaction(transactionManager, () -> {
 *     // 执行数据库操作
 *     return userService.createUser();
 * });
 *
 * // 自定义事务配置示例（设置隔离级别和传播行为）
 * TransactionDefinition def = TransactionUtil.defineTransaction(
 *     IsolationLevel.READ_COMMITTED,
 *     PropagationBehavior.REQUIRES_NEW,
 *     10,
 *     false);
 * TransactionUtil.transaction(transactionManager, () -> {
 *     return orderService.processOrder();
 * }, def);
 * }</pre>
 * @author LBY
 */
public class TransactionUtil {

    private static final Logger logger = LoggerFactory.getLogger(TransactionUtil.class);

    /**
     * 简化版事务执行方法（使用默认配置）<pre>{@code
     * // 示例：创建用户操作（自动管理事务）
     * User user = TransactionUtil.transaction(transactionManager, () -> {
     *     userRepo.save(newUser);
     *     return newUser;
     * });
     * }</pre>
     *
     * @param transactionManager 事务管理器（不能为空）
     * @param action             事务操作（不能为空，需实现TransactionCallback接口）
     * @param <T>                操作返回类型
     * @return 事务执行结果（若失败则抛出RuntimeException）
     * @throws IllegalArgumentException 如果参数为null或配置非法
     */
    public static <T> T transaction(PlatformTransactionManager transactionManager, TransactionCallback<T> action) {
        return transaction(transactionManager, action, null);
    }

    /**
     * 完全配置的事务执行方法<pre>{@code
     * // 示例：带超时和隔离级别的订单处理
     * TransactionDefinition def = TransactionUtil.defineTransaction(
     *     IsolationLevel.SERIALIZABLE,
     *     PropagationBehavior.NESTED,
     *     30,
     *     false);
     * Order result = TransactionUtil.transaction(transactionManager, () -> {
     *     return orderService.processComplexOrder();
     * }, def);
     * }</pre>
     *
     * @param transactionManager 事务管理器（不能为空）
     * @param action             事务操作（不能为空，需实现TransactionCallback接口）
     * @param definition         事务定义（可选，若为null则使用默认配置）
     * @return 事务执行结果（若失败则抛出RuntimeException）
     * @throws IllegalArgumentException 如果transactionManager或action为null
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
            logger.debug("事务配置 - 隔离级别: {}, 传播行为: {}, 超时: {}秒, 只读: {}",
                    definition.getIsolationLevel(),
                    definition.getPropagationBehavior(),
                    definition.getTimeout(),
                    definition.isReadOnly());
            // 记录事务开始日志
            logger.info("开始事务");
            // 执行事务并获取结果
            T result = template.execute(action);
            // 记录事务成功完成日志
            logger.info("事务成功完成");
            // 返回事务执行结果
            return result;
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            } else {
                throw new RuntimeException("事务执行失败", e);
            }
        }
    }

    /**
     * 创建事务配置对象
     * <pre>{@code
     * // 示例：创建只读事务配置（用于查询操作）
     * TransactionDefinition readOnlyDef = TransactionUtil.defineTransaction(
     *     IsolationLevel.READ_UNCOMMITTED,
     *     PropagationBehavior.SUPPORTS,
     *     0,
     *     true);
     *
     * // 示例：设置超时警告（超过30秒会触发日志警告）
     * TransactionDefinition longDef = TransactionUtil.defineTransaction(
     *     IsolationLevel.READ_COMMITTED,
     *     PropagationBehavior.REQUIRED,
     *     60,
     *     false);
     * }</pre>
     *
     * @param isolationLevel 事务隔离级别（不可为空，例如IsolationLevel.READ_COMMITTED）
     * @param propagation    事务传播行为（不可为空，例如PropagationBehavior.REQUIRED）
     * @param timeoutSeconds 超时时间（秒，必须≥0，超过30秒会触发日志警告）
     * @param readOnly       是否只读模式（true时优化性能，建议用于查询场景）
     * @return 配置好的事务定义对象
     * @throws IllegalArgumentException 如果参数无效（如隔离级别为空或超时为负数）
     */
    public static TransactionDefinition defineTransaction(IsolationLevel isolationLevel,
                                                          PropagationBehavior propagation,
                                                          int timeoutSeconds,
                                                          boolean readOnly) {
        // 参数校验
        if (isolationLevel == null) {
            throw new IllegalArgumentException("隔离级别不能为空");
        }
        if (propagation == null) {
            throw new IllegalArgumentException("传播行为不能为空");
        }
        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException("超时时间不能为负数，当前值：" + timeoutSeconds);
        }
        if (timeoutSeconds > 300) {
            logger.warn("检测到过长超时设置：{}秒，建议事务执行时间不超过30秒", timeoutSeconds);
        }
        DefaultTransactionDefinition definition = new DefaultTransactionDefinition();
        definition.setIsolationLevel(isolationLevel.getValue());
        definition.setPropagationBehavior(propagation.getValue());
        definition.setTimeout(timeoutSeconds);
        definition.setReadOnly(readOnly);
        return definition;
    }

}