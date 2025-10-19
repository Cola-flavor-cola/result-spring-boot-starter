package com.lby.result.utils;

import com.alibaba.fastjson2.JSON;
import com.lby.result.enums.TimeType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.messaging.support.MessageBuilder;

import java.util.function.Supplier;

/**
 * Rabbit 类用于封装 RabbitMQ 的常用操作。
 * 包括发送消息、接收消息以及设置确认和返回回调。
 */
@Slf4j
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class Rabbit<T> {

    /**
     * 自动注入的 RabbitTemplate 实例，用于与 RabbitMQ 进行通信。
     */
    private final RabbitTemplate rabbitTemplate;
    // 新增常量定义
    private static final String PARAM_EMPTY_MSG_TEMPLATE = "无效的参数: %s 不能为空";

    // 定义常量，用于记录参数为空的错误信息
    private static final String EXCHANGE_EMPTY_MSG = String.format(PARAM_EMPTY_MSG_TEMPLATE, "exchange");
    private static final String ROUTING_KEY_EMPTY_MSG = String.format(PARAM_EMPTY_MSG_TEMPLATE, "routingKey");

    private static final String QUEUE_EMPTY_MSG = String.format(PARAM_EMPTY_MSG_TEMPLATE, "queue");

    // 原始方法，使用 T message 参数

    /**
     * 向指定的交换器和路由键发送消息。
     *
     * <h3>方法功能</h3>
     * 将消息发送到指定的交换器，并通过路由键进行路由。支持以下特性：
     * <ul>
     *     <li>支持消息体为null的发送</li>
     *     <li>自动处理消息序列化</li>
     *     <li>集成消息发送异常处理机制</li>
     * </ul>
     *
     * <h3>参数说明</h3>
     * <table border="1">
     *     <tr><th>参数</th><th>说明</th></tr>
     *     <tr><td>exchange</td>     <td>目标交换器名称（必填）</td></tr>
     *     <tr><td>routingKey</td>   <td>路由键（必填）</td></tr>
     *     <tr><td>message</td>      <td>消息体对象（可为null）</td></tr>
     * </table>
     *
     * <h3>使用示例</h3>
     * <pre>
     *     {@code
     *      // 基础用法：发送普通消息
     * rabbit.sendDirect(
     *     "order_exchange",          // 交换器名称
     *     "order.created",           // 路由键
     *     new OrderMessage(123L)     // 消息体对象
     * );
     *
     * // 发送空消息示例
     * rabbit.sendDirect(
     *     "notification_exchange",
     *     "user.login",
     *     null                      // 允许发送空消息
     * );
     * }
     * </pre>
     *
     * <h3>异常处理</h3>
     * <ul>
     *     <li>参数校验异常：当交换器或路由键为空时抛出{@link IllegalArgumentException}</li>
     *     <li>连接异常：当RabbitMQ连接失败时抛出{@link AmqpConnectException}</li>
     *     <li>发送异常：消息发送失败时抛出{@link AmqpException}</li>
     * </ul>
     */
    public void sendDirect(String exchange, String routingKey, @Nullable T message) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 调用内部发送方法
        send(exchange, routingKey, message, null, null, null);
    }

    /**
     * 向指定的交换器和路由键发送消息，并携带关联数据。
     *
     * @param exchange        交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey      路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param message         要发送的消息对象。
     * @param correlationData 关联数据，用于消息确认和追踪（可为null）
     *
     *                        <pre>{@code
     *                                                                                                                    // 发送带关联数据的消息
     *                                                                                                                    CorrelationData cd = new CorrelationData("MSG_20231001_001");
     *                                                                                                                    rabbit.sendDirect("order_exchange", "create", new Order(), cd);
     *
     *                                                                                                                    // 关联数据可选参数（允许传null）
     *                                                                                                                    rabbit.sendDirect("log_exchange", "info", "系统日志", null);
     *
     *                                                                                                                    // 参数校验失败示例（交换器为空）
     *                                                                                                                    try {
     *                                                                                                                        rabbit.sendDirect("", "key", "无效消息", new CorrelationData("invalid"));
     *                                                                                                                    } catch (IllegalArgumentException e) {
     *                                                                                                                        System.out.println(e.getMessage());
     *                                                                                                                        // 输出"无效的参数: exchange 不能为空"
     *                                                                                                                    }
     *                                                                                                                    }</pre>
     */
    public void sendDirect(String exchange, String routingKey, @Nullable T message, CorrelationData correlationData) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 调用内部发送方法
        send(exchange, routingKey, message, correlationData, null, null);
    }

    /**
     * 向指定的交换器和路由键发送延迟消息，默认单位为秒。
     *
     * @param exchange   交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param message    要发送的消息对象（可为null）。
     * @param time       消息的延迟时间（单位：秒），必须≥0。若为null则不启用延迟。
     *
     *                   <pre>{@code
     *                                                                                           // 发送5秒延迟的消息
     *                                                                                           rabbit.sendDirect("delay_exchange", "task", "定时任务", 5);
     *
     *                                                                                           // 允许消息体为null的场景
     *                                                                                           rabbit.sendDirect("log_exchange", "info", null, 10);
     *
     *                                                                                           // 参数校验失败示例（时间参数为负数）
     *                                                                                           try {
     *                                                                                               rabbit.sendDirect("ex", "key", "msg", -3);
     *                                                                                           } catch (IllegalArgumentException e) {
     *                                                                                               System.out.println(e.getMessage());
     *                                                                                               // 输出"delayTime和timeType必须同时设置或同时为null"
     *                                                                                           }
     *                                                                                           }</pre>
     */
    public void sendDirect(String exchange, String routingKey, @Nullable T message, Integer time) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 调用内部发送方法，默认延迟时间为秒
        send(exchange, routingKey, message, null, time, TimeType.SECONDS);
    }

    /**
     * 发送带延迟的消息（延迟5秒）
     *
     * <pre>{@code
     * // 发送5秒后执行的消息
     * rabbit.sendDirect("delay_exchange", "task", "定时任务", 5, TimeType.SECONDS);
     *
     * // 不合法的参数组合（会抛出IllegalArgumentException）
     * try {
     *     rabbit.sendDirect("ex", "key", "msg", 10, null);
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"delayTime和timeType必须同时设置或同时为null"
     * }
     * }</pre>
     */
    public void sendDirect(String exchange, String routingKey, @Nullable T message, Integer time, TimeType timeUnit) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 调用内部发送方法，指定延迟时间和时间单位
        send(exchange, routingKey, message, null, time, timeUnit);
    }

    /**
     * 向指定的队列发送消息。
     *
     * @param queue   队列名称，必须有效且不可为空。
     * @param message 要发送的消息对象（可为null，允许空值）。
     *
     *                <pre>{@code
     *                                                                            // 正常发送消息到队列
     *                                                                            rabbit.sendQueue("user_queue", new User("Alice", "123456"));
     *
     *                                                                            // 允许消息为空的场景
     *                                                                            rabbit.sendQueue("log_queue", null);
     *
     *                                                                            // 参数校验失败示例（队列名称为空）
     *                                                                            try {
     *                                                                                rabbit.sendQueue("", "无效队列");
     *                                                                            } catch (IllegalArgumentException e) {
     *                                                                                System.out.println(e.getMessage());
     *                                                                                // 输出"无效的参数: queue 不能为空"
     *                                                                            }
     *                                                                            }</pre>
     */
    public void sendQueue(String queue, @Nullable T message) {
        // 验证队列是否有效
        validateQueue(queue);
        try {
            // 使用 RabbitTemplate 发送消息到指定队列
            rabbitTemplate.convertAndSend(queue, message);
            // 记录发送成功的日志
            logIfEnabled(LogLevel.INFO, LogMessages.SEND_SUCCESS_QUEUE, JSON.toJSONString(message));
        } catch (AmqpException e) {
            // 记录发送失败的日志
            logIfEnabled(LogLevel.ERROR, LogMessages.SEND_FAILURE_QUEUE, JSON.toJSONString(message), e);
            // 抛出异常
            throw new AmqpException(LogMessages.SEND_FAILURE, e);
        }
    }

    /**
     * 使用 RabbitMQ 发送扇形（Fanout）消息。
     * 扇形交换器会将接收到的消息广播到所有绑定到它的队列中。
     * 此方法负责将消息发送到指定的扇形交换器，不需要指定路由键。
     *
     * @param exchange 交换器名称，必须有效且不可为空。
     * @param message  要发送的消息内容（可为null，允许空值），需要与序列化方式匹配。
     *
     *                 <pre>{@code
     *                                                                                 // 发送字符串消息到扇形交换器
     *                                                                                 rabbit.sendFanout("log_fanout", "系统日志消息");
     *
     *                                                                                 // 发送复杂对象消息
     *                                                                                 rabbit.sendFanout("user_fanout", new User("Alice", "123456"));
     *
     *                                                                                 // 允许消息为空的场景
     *                                                                                 rabbit.sendFanout("empty_fanout", null);
     *
     *                                                                                 // 参数校验失败示例（交换器名称为空）
     *                                                                                 try {
     *                                                                                     rabbit.sendFanout("", "无效交换器");
     *                                                                                 } catch (IllegalArgumentException e) {
     *                                                                                     System.out.println(e.getMessage());
     *                                                                                     // 输出"无效的参数: exchange 不能为空"
     *                                                                                 }
     *                                                                                 }</pre>
     */
    public void sendFanout(String exchange, @Nullable T message) {
        // 验证交换器是否有效
        validateExchange(exchange);
        try {
            // 使用 RabbitTemplate 发送消息到指定的扇形交换器
            rabbitTemplate.convertAndSend(exchange, null, message);
            // 记录发送成功的日志
            logIfEnabled(LogLevel.INFO, LogMessages.SEND_SUCCESS_FANOUT, message.getClass().getSimpleName());
        } catch (AmqpException e) {
            // 记录发送失败的日志
            logIfEnabled(LogLevel.ERROR, LogMessages.SEND_FAILURE_FANOUT, e);
            // 抛出异常
            throw new AmqpException(LogMessages.SEND_FAILURE, e);
        }
    }

    /**
     * 向指定的交换器和路由键发送消息。
     *
     * @param exchange   交换器名称，决定了消息将被发送到哪个交换器。必须有效且不可为空。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。必须有效且不可为空。
     * @param message    要发送的消息对象（可为null，允许空值）。
     *
     *                   <pre>{@code
     *                                                                                           // 正常发送消息到Topic交换器
     *                                                                                           rabbit.sendTopic("topic_exchange", "key.pattern", new Event("event1"));
     *
     *                                                                                           // 允许消息为空的场景
     *                                                                                           rabbit.sendTopic("log_exchange", "info", null);
     *
     *                                                                                           // 参数校验失败示例（交换器或路由键为空）
     *                                                                                           try {
     *                                                                                               rabbit.sendTopic("", "key", "无效交换器");
     *                                                                                           } catch (IllegalArgumentException e) {
     *                                                                                               System.out.println(e.getMessage());
     *                                                                                               // 输出"无效的参数: exchange 不能为空"
     *                                                                                           }
     *                                                                                           }</pre>
     */
    public void sendTopic(String exchange, String routingKey, @Nullable T message) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 调用内部发送方法
        send(exchange, routingKey, message, null, null, null);
    }

    // 新增方法，使用 Supplier<T> message 参数

    /**
     * 向指定的交换器和路由键发送消息，使用 Lambda 表达式延迟创建消息对象。
     *
     * @param exchange        交换器名称，决定了消息将被发送到哪个交换器。必须有效且不可为空。
     * @param routingKey      路由键，与交换器类型一起决定消息将被路由到哪个队列。必须有效且不可为空。
     * @param messageSupplier 提供消息对象的 Lambda 表达式（可为null，允许空值）。
     *
     *                        <pre>{@code
     *                                                                                                                    // 正常发送消息到Direct交换器，使用Lambda表达式提供消息
     *                                                                                                                    rabbit.sendDirect("direct_exchange", "key", () -> new Event("event1"));
     *
     *                                                                                                                    // 允许消息为空的场景，使用Lambda表达式提供null
     *                                                                                                                    rabbit.sendDirect("log_exchange", "info", () -> null);
     *
     *                                                                                                                    // 参数校验失败示例（交换器或路由键为空）
     *                                                                                                                    try {
     *                                                                                                                        rabbit.sendDirect("", "key", () -> "无效交换器");
     *                                                                                                                    } catch (IllegalArgumentException e) {
     *                                                                                                                        System.out.println(e.getMessage());
     *                                                                                                                        // 输出"无效的参数: exchange 不能为空"
     *                                                                                                                    }
     *                                                                                                                    }</pre>
     */
    public void sendDirect(String exchange, String routingKey, @Nullable Supplier<T> messageSupplier) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 获取消息对象
        T message = messageSupplier.get();
        // 调用内部发送方法
        send(exchange, routingKey, message, null, null, null);
    }

    /**
     * 向指定的交换器和路由键发送消息，并携带关联数据，使用 Lambda 表达式延迟创建消息对象。
     *
     * @param exchange        交换器名称，决定了消息将被发送到哪个交换器。必须有效且不可为空。
     * @param routingKey      路由键，与交换器类型一起决定消息将被路由到哪个队列。必须有效且不可为空。
     * @param messageSupplier 提供消息对象的 Lambda 表达式（可为null，允许空值）。
     * @param correlationData 关联数据，用于确认消息的唯一标识符等信息（可为null）。
     *
     *                        <pre>{@code
     *                                                                                                                    // 正常发送消息到Direct交换器，使用Lambda表达式提供消息并携带关联数据
     *                                                                                                                    rabbit.sendDirect("direct_exchange", "key", () -> new Event("event1"), new CorrelationData("id1"));
     *
     *                                                                                                                    // 允许消息为空的场景，使用Lambda表达式提供null并携带关联数据
     *                                                                                                                    rabbit.sendDirect("log_exchange", "info", () -> null, new CorrelationData("id2"));
     *
     *                                                                                                                    // 参数校验失败示例（交换器或路由键为空）
     *                                                                                                                    try {
     *                                                                                                                        rabbit.sendDirect("", "key", () -> "无效交换器", null);
     *                                                                                                                    } catch (IllegalArgumentException e) {
     *                                                                                                                        System.out.println(e.getMessage());
     *                                                                                                                        // 输出"无效的参数: exchange 不能为空"
     *                                                                                                                    }
     *                                                                                                                    }</pre>
     */
    public void sendDirect(String exchange, String routingKey, @Nullable Supplier<T> messageSupplier, CorrelationData correlationData) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 获取消息对象
        T message = messageSupplier.get();
        // 调用内部发送方法
        send(exchange, routingKey, message, correlationData, null, null);
    }

    /**
     * 向指定的交换器和路由键发送延迟消息，默认单位为秒，使用 Lambda 表达式延迟创建消息对象。
     *
     * @param exchange        交换器名称，决定了消息将被发送到哪个交换器。必须有效且不可为空。
     * @param routingKey      路由键，与交换器类型一起决定消息将被路由到哪个队列。必须有效且不可为空。
     * @param messageSupplier 提供消息对象的 Lambda 表达式（可为null，允许空值）。
     * @param time            消息的延迟时间，单位为秒。必须为非负数。
     *
     *                        <pre>{@code
     *                                                                                                                    // 正常发送延迟消息到Direct交换器，使用Lambda表达式提供消息并设置延迟时间
     *                                                                                                                    rabbit.sendDirect("direct_exchange", "key", () -> new Event("event1"), 5);
     *
     *                                                                                                                    // 允许消息为空的场景，使用Lambda表达式提供null并设置延迟时间
     *                                                                                                                    rabbit.sendDirect("log_exchange", "info", () -> null, 10);
     *
     *                                                                                                                    // 参数校验失败示例（交换器或路由键为空）
     *                                                                                                                    try {
     *                                                                                                                        rabbit.sendDirect("", "key", () -> "无效交换器", 5);
     *                                                                                                                    } catch (IllegalArgumentException e) {
     *                                                                                                                        System.out.println(e.getMessage());
     *                                                                                                                        // 输出"无效的参数: exchange 不能为空"
     *                                                                                                                    }
     *                                                                                                                    }</pre>
     */
    public void sendDirect(String exchange, String routingKey, @Nullable Supplier<T> messageSupplier, Integer time) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 获取消息对象
        T message = messageSupplier.get();
        // 调用内部发送方法，默认延迟时间为秒
        send(exchange, routingKey, message, null, time, TimeType.SECONDS);
    }

    /**
     * 向指定的交换器和路由键发送延迟消息，可以指定时间单位，使用 Lambda 表达式延迟创建消息对象。
     *
     * @param exchange        交换器名称，决定了消息将被发送到哪个交换器。必须有效且不可为空。
     * @param routingKey      路由键，与交换器类型一起决定消息将被路由到哪个队列。必须有效且不可为空。
     * @param messageSupplier 提供消息对象的 Lambda 表达式（可为null，允许空值）。
     * @param time            延迟时间。必须为非负数。
     * @param timeUnit        延迟时间的时间单位。必须有效且不可为空。
     *
     *                        <pre>{@code
     *                                                                                                                    // 正常发送延迟消息到Direct交换器，使用Lambda表达式提供消息并设置延迟时间和单位
     *                                                                                                                    rabbit.sendDirect("direct_exchange", "key", () -> new Event("event1"), 5, TimeType.SECONDS);
     *
     *                                                                                                                    // 允许消息为空的场景，使用Lambda表达式提供null并设置延迟时间和单位
     *                                                                                                                    rabbit.sendDirect("log_exchange", "info", () -> null, 10, TimeType.MINUTES);
     *
     *                                                                                                                    // 参数校验失败示例（交换器或路由键为空）
     *                                                                                                                    try {
     *                                                                                                                        rabbit.sendDirect("", "key", () -> "无效交换器", 5, TimeType.SECONDS);
     *                                                                                                                    } catch (IllegalArgumentException e) {
     *                                                                                                                        System.out.println(e.getMessage());
     *                                                                                                                        // 输出"无效的参数: exchange 不能为空"
     *                                                                                                                    }
     *                                                                                                                    }</pre>
     */
    public void sendDirect(String exchange, String routingKey, @Nullable Supplier<T> messageSupplier, Integer time, TimeType timeUnit) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 获取消息对象
        T message = messageSupplier.get();
        // 调用内部发送方法，指定延迟时间和时间单位
        send(exchange, routingKey, message, null, time, timeUnit);
    }

    /**
     * 向指定的队列发送消息，使用 Lambda 表达式延迟创建消息对象。
     *
     * @param queue           队列名称，表示消息将被发送到的队列。必须有效且不可为空。
     * @param messageSupplier 提供消息对象的 Lambda 表达式（可为null，允许空值）。
     *
     *                        <pre>{@code
     *                                                                                                                    // 正常发送消息到指定队列，使用Lambda表达式提供消息
     *                                                                                                                    rabbit.sendQueue("my_queue", () -> new Event("event1"));
     *
     *                                                                                                                    // 允许消息为空的场景，使用Lambda表达式提供null
     *                                                                                                                    rabbit.sendQueue("log_queue", () -> null);
     *
     *                                                                                                                    // 参数校验失败示例（队列为空）
     *                                                                                                                    try {
     *                                                                                                                        rabbit.sendQueue("", () -> "无效队列");
     *                                                                                                                    } catch (IllegalArgumentException e) {
     *                                                                                                                        System.out.println(e.getMessage());
     *                                                                                                                        // 输出"无效的参数: queue 不能为空"
     *                                                                                                                    }
     *                                                                                                                    }</pre>
     */
    public void sendQueue(String queue, @Nullable Supplier<T> messageSupplier) {
        // 验证队列是否有效
        validateQueue(queue);
        try {
            // 获取消息对象
            T message = messageSupplier.get();
            // 使用 RabbitTemplate 发送消息到指定队列
            rabbitTemplate.convertAndSend(queue, message);
            // 记录发送成功的日志
            logIfEnabled(LogLevel.INFO, LogMessages.SEND_SUCCESS_QUEUE, JSON.toJSONString(message));
        } catch (AmqpException e) {
            // 记录发送失败的日志
            logIfEnabled(LogLevel.ERROR, LogMessages.SEND_FAILURE_QUEUE, JSON.toJSONString(messageSupplier.get()), e);
            // 抛出异常
            throw new AmqpException(LogMessages.SEND_FAILURE, e);
        }
    }

    /**
     * 使用 RabbitMQ 发送扇形（Fanout）消息，使用 Lambda 表达式延迟创建消息对象。
     * 扇形交换器会将接收到的消息广播到所有绑定到它的队列中。
     * 此方法负责将消息发送到指定的扇形交换器，不需要指定路由键。
     *
     * @param exchange        交换器名称，消息将被发送到这个交换器。必须有效且不可为空。
     * @param messageSupplier 提供消息对象的 Lambda 表达式（可为null，允许空值）。
     *
     *                        <pre>{@code
     *                                                                                                                    // 正常发送扇形消息，使用Lambda表达式提供消息
     *                                                                                                                    rabbit.sendFanout("fanout_exchange", () -> new Event("event1"));
     *
     *                                                                                                                    // 允许消息为空的场景，使用Lambda表达式提供null
     *                                                                                                                    rabbit.sendFanout("log_exchange", () -> null);
     *
     *                                                                                                                    // 参数校验失败示例（交换器为空）
     *                                                                                                                    try {
     *                                                                                                                        rabbit.sendFanout("", () -> "无效交换器");
     *                                                                                                                    } catch (IllegalArgumentException e) {
     *                                                                                                                        System.out.println(e.getMessage());
     *                                                                                                                        // 输出"无效的参数: exchange 不能为空"
     *                                                                                                                    }
     *                                                                                                                    }</pre>
     */
    public void sendFanout(String exchange, @Nullable Supplier<T> messageSupplier) {
        // 验证交换器是否有效
        validateExchange(exchange);
        try {
            // 获取消息对象
            T message = messageSupplier.get();
            // 使用 RabbitTemplate 发送消息到指定的扇形交换器
            rabbitTemplate.convertAndSend(exchange, null, message);
            // 记录发送成功的日志
            log.info(LogMessages.SEND_SUCCESS_FANOUT, message.getClass().getSimpleName());
        } catch (AmqpException e) {
            // 记录发送失败的日志
            log.error(LogMessages.SEND_FAILURE_FANOUT, e);
            // 抛出异常
            throw new AmqpException(LogMessages.SEND_FAILURE, e);
        }
    }

    /**
     * 向指定的交换器和路由键发送消息，使用 Lambda 表达式延迟创建消息对象。
     *
     * @param exchange        交换器名称，决定了消息将被发送到哪个交换器。必须有效且不可为空。
     * @param routingKey      路由键，与交换器类型一起决定消息将被路由到哪个队列。必须有效且不可为空。
     * @param messageSupplier 提供消息对象的 Lambda 表达式（可为null，允许空值）。
     *
     *                        <pre>{@code
     *                                                                                                                    // 正常发送消息到指定的Topic交换器，使用Lambda表达式提供消息
     *                                                                                                                    rabbit.sendTopic("topic_exchange", "key", () -> new Event("event1"));
     *
     *                                                                                                                    // 允许消息为空的场景，使用Lambda表达式提供null
     *                                                                                                                    rabbit.sendTopic("log_exchange", "info", () -> null);
     *
     *                                                                                                                    // 参数校验失败示例（交换器或路由键为空）
     *                                                                                                                    try {
     *                                                                                                                        rabbit.sendTopic("", "key", () -> "无效交换器");
     *                                                                                                                    } catch (IllegalArgumentException e) {
     *                                                                                                                        System.out.println(e.getMessage());
     *                                                                                                                        // 输出"无效的参数: exchange 不能为空"
     *                                                                                                                    }
     *                                                                                                                    }</pre>
     */
    public void sendTopic(String exchange, String routingKey, @Nullable Supplier<T> messageSupplier) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 获取消息对象
        T message = messageSupplier.get();
        // 调用内部发送方法
        send(exchange, routingKey, message, null, null, null);
    }

    /**
     * 发送消息到指定交换机，支持延迟消息和消息追踪
     *
     * @param exchange        交换机名称，不可为空
     * @param routingKey      路由键，用于消息路由
     * @param message         消息体，可为空。使用@Nullable注解标记允许空值
     * @param correlationData 消息关联数据，用于生产者确认机制
     * @param delayTime       延迟时间数值，需与timeType配合使用。null表示不延迟
     * @param timeType        时间单位类型，需与delayTime配合使用。null表示不延迟
     * @throws IllegalArgumentException 当delayTime和timeType参数设置不一致时抛出
     * @throws AmqpConnectException     当连接RabbitMQ失败时抛出
     * @throws AmqpException            当消息发送失败时抛出
     */
    private void send(String exchange, String routingKey, @Nullable T message, CorrelationData correlationData, Integer delayTime, TimeType timeType) {
        // 参数一致性校验：delayTime和timeType必须同时存在或同时为null
        if ((delayTime != null && timeType == null) || (delayTime == null && timeType != null)) {
            throw new IllegalArgumentException(LogMessages.DELAY_TIME_AND_TIME_TYPE_MUST_BE_SET_OR_NULL);
        }

        try {
            // 构建消息后处理器：处理延迟设置
            MessagePostProcessor processor = message1 -> {
                if (delayTime != null) {
                    long delay = timeType.toMilliseconds(delayTime);
                    message1.getMessageProperties().setDelayLong(delay);
                }
                return message1;
            };

            // 执行消息发送操作
            rabbitTemplate.convertAndSend(exchange, routingKey, message, processor, correlationData);

            // 记录成功日志（仅在日志级别启用时）
            logIfEnabled(LogLevel.INFO, LogMessages.SEND_SUCCESS_TEMPLATE,
                    exchange, routingKey, getMaskedMessage(message));
        } catch (AmqpConnectException e) {
            // 连接异常处理：记录详细错误日志并重新抛出异常
            String safeMessage = message != null ? JSON.toJSONString(message) : "null";
            logIfEnabled(LogLevel.ERROR, LogMessages.CONNECTION_FAILURE, safeMessage, e);
            throw new AmqpConnectException("连接失败: " + e.getMessage(), e);
        } catch (AmqpException e) {
            // 消息发送异常处理：记录上下文信息并重新抛出异常
            logIfEnabled(LogLevel.ERROR, LogMessages.SEND_FAILURE_DETAILS,
                    exchange, routingKey, getMaskedMessage(message), e.getClass().getSimpleName(), e.getMessage());
            throw new AmqpException("发送失败: " + e.getMessage(), e);
        }
    }

    /**
     * 验证交换器是否有效。
     *
     * @param exchange 交换器名称。
     */
    private void validateExchange(String exchange) {
        if (exchange == null || exchange.isBlank()) {
            throw new IllegalArgumentException(EXCHANGE_EMPTY_MSG);
        }
    }


    /**
     * 验证路由键是否有效。
     *
     * @param routingKey 路由键。
     */
    private void validateRoutingKey(String routingKey) {
        if (routingKey == null || routingKey.isBlank()) {
            throw new IllegalArgumentException(ROUTING_KEY_EMPTY_MSG);
        }
    }

    /**
     * 验证交换器和路由键是否有效。
     *
     * @param exchange   交换器名称。
     * @param routingKey 路由键。
     */
    private void validateExchangeAndRoutingKey(String exchange, String routingKey) {
        // 验证交换器
        validateExchange(exchange);
        // 验证路由键
        validateRoutingKey(routingKey);
    }

    /**
     * 验证队列是否有效。
     *
     * @param queue 队列名称。
     */
    private void validateQueue(String queue) {
        if (queue == null || queue.isBlank()) {
            throw new IllegalArgumentException(QUEUE_EMPTY_MSG);
        }
    }

    /**
     * 将给定的消息对象转换为 JSON 字符串，并掩盖敏感数据。
     *
     * @param message 要转换和掩盖的原始消息对象。
     * @return 转换为 JSON 字符串并掩盖敏感数据后的消息。
     */
    private String getMaskedMessage(T message) {
        return maskSensitiveData(JSON.toJSONString(message));
    }

    /**
     * 对敏感数据进行脱敏处理。
     *
     * @param data 待脱敏处理的字符串，可能包含敏感信息。
     * @return 脱敏后的字符串，其中敏感信息被部分隐藏。
     */
    private String maskSensitiveData(String data) {
        // 简单的脱敏处理示例，可以根据实际需求进行更复杂的处理
        return data.replaceAll("(?i)(password|token|secret)=\\w+", "$1=****");
    }

    /**
     * 记录发送失败的日志。
     *
     * @param exchange     交换器名称。
     * @param routingKey   路由键。
     * @param message      消息对象。
     * @param errorMessage 错误信息。
     * @param e            异常对象。
     */
    private void logFailure(String exchange, String routingKey, T message, String errorMessage, Exception e) {
        log.error(errorMessage + " [exchange={}, routingKey={}, message={}]", exchange, routingKey, getMaskedMessage(message), e);
    }

    /**
     * 定义日志级别枚举。
     */
    private enum LogLevel {
        INFO,
        DEBUG,
        ERROR
    }

    /**
     * 日志消息模板静态内部类，用于统一管理RabbitMQ操作相关的日志消息格式
     */
    private static class LogMessages {
        /**
         * 队列消息发送成功模板，参数为队列标识
         */
        private static final String SEND_SUCCESS_QUEUE = "发送消息成功===>{}";
        /**
         * 队列消息发送失败模板，参数为错误详情
         */
        private static final String SEND_FAILURE_QUEUE = "发送消息失败===>{}";
        /**
         * Fanout消息发送成功模板，参数为交换机标识
         */
        private static final String SEND_SUCCESS_FANOUT = "发送消息成功==>{}";
        /**
         * Fanout消息发送失败通用模板
         */
        private static final String SEND_FAILURE_FANOUT = "发送消息失败";
        /**
         * 模板化消息发送成功日志模板（参数顺序：交换机名称、路由键、消息体）
         */
        private static final String SEND_SUCCESS_TEMPLATE = "发送消息成功 [exchange={}, routingKey={}, message={}]";
        /**
         * 连接失败基础模板，参数为错误详情
         */
        private static final String CONNECTION_FAILURE = "连接RabbitMQ失败==>{}";
        /**
         * 发送失败详细模板（参数顺序：交换机名称、路由键、消息体、错误类型、错误详情）
         */
        private static final String SEND_FAILURE_DETAILS = "发送消息失败 [exchange={}, routingKey={}, message={}] - {} : {}";
        /**
         * 发送失败详细模板（参数顺序：交换机名称、路由键、消息体、错误类型、错误详情）
         */
        private static final String SEND_FAILURE_DETAILS_DETAILS = "发送消息失败 [exchange={}, routingKey={}, message={}] - {} : {}";
        /**
         * 发送失败详细模板（参数顺序：交换机名称、路由键、消息体、错误类型、错误详情）
         */
        private static final String SEND_FAILURE_DETAILS_DETAILS_DETAILS = "发送消息失败 [exchange={}, routingKey={}, message={}] - {} : {}";
        /**
         * 通用发送失败简短提示
         */
        private static final String SEND_FAILURE = "发送失败";
        /**
         * 连接失败详细模板，参数为错误详情
         */
        private static final String CONNECTION_FAILURE_DETAILS = "连接RabbitMQ失败==>{}";
        /**
         * 延迟消息参数校验失败提示模板
         */
        private static final String DELAY_TIME_AND_TIME_TYPE_MUST_BE_SET_OR_NULL = "delayTime和timeType必须同时设置或同时为null";
    }

    /**
     * 根据日志级别记录日志。
     *
     * @param level     日志级别。
     * @param format    格式化的日志消息。
     * @param arguments 日志消息中的参数。
     */
    private void logIfEnabled(LogLevel level, String format, Object... arguments) {
        if (level == null) {
            throw new IllegalArgumentException("日志级别不能为null");
        }
        switch (level) {
            case INFO:
                log.info(format, arguments);
                break;
            case DEBUG:
                log.debug(format, arguments);
                break;
            case ERROR:
                log.error(format, arguments);
                break;
            default:
                throw new IllegalArgumentException("不支持的日志级别: " + level);
        }
    }

}