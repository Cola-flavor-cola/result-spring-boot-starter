package com.lby.result.utils;

import com.alibaba.fastjson2.JSON;
import com.lby.result.enums.TimeType;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpConnectException;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.connection.CorrelationData;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import reactor.core.publisher.Flux;

import java.util.function.Supplier;
import java.util.concurrent.TimeUnit;

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

    // 原始方法，使用 T message 参数

    /**
     * 向指定的交换器和路由键发送消息。
     *
     * @param exchange   交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param message    要发送的消息对象。
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
     * @param correlationData 关联数据，用于确认消息的唯一标识符等信息。
     */
    public void sendDirect(String exchange, String routingKey,@Nullable T message, CorrelationData correlationData) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 调用内部发送方法
        send(exchange, routingKey, message, correlationData, null, null);
    }

    /**
     * 向指定的交换器和路由键发送延迟消息，默认单位为秒。
     *
     * @param exchange 交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param message 发送的消息对象。
     * @param time 消息的延迟时间，单位为秒。
     */
    public void sendDirect(String exchange, String routingKey,@Nullable T message, Integer time) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 调用内部发送方法，默认延迟时间为秒
        send(exchange, routingKey, message, null, time, TimeType.SECONDS);
    }

    /**
     * 向指定的交换器和路由键发送延迟消息，可以指定时间单位。
     *
     * @param exchange 交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param message 发送的消息对象。
     * @param time 延迟时间。
     * @param timeUnit 延迟时间的时间单位。
     */
    public void sendDirect(String exchange, String routingKey,@Nullable T message, Integer time, TimeType timeUnit) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 调用内部发送方法，指定延迟时间和时间单位
        send(exchange, routingKey, message, null, time, timeUnit);
    }

    /**
     * 向指定的队列发送消息。
     *
     * @param queue 队列名称，表示消息将被发送到的队列。
     * @param message 发送的消息对象。
     */
    public void sendQueue(String queue,@Nullable T message) {
        // 验证队列是否有效
        validateQueue(queue);
        try {
            // 使用 RabbitTemplate 发送消息到指定队列
            rabbitTemplate.convertAndSend(queue, message);
            // 记录发送成功的日志
            logIfEnabled(LogLevel.INFO, "发送消息成功===>{}", JSON.toJSONString(message));
        } catch (AmqpException e) {
            // 记录发送失败的日志
            logIfEnabled(LogLevel.ERROR, "发送消息失败===>{}", JSON.toJSONString(message), e);
            // 抛出异常
            throw new AmqpException("发送失败", e);
        }
    }

    /**
     * 使用 RabbitMQ 发送扇形（Fanout）消息。
     * 扇形交换器会将接收到的消息广播到所有绑定到它的队列中。
     * 此方法负责将消息发送到指定的扇形交换器，不需要指定路由键。
     *
     * @param exchange 交换器名称，消息将被发送到这个交换器。
     * @param message 要发送的消息内容，可以是任意类型，但需要与消息序列化方式匹配。
     */
    public void sendFanout(String exchange,@Nullable T message) {
        // 验证交换器是否有效
        validateExchange(exchange);
        try {
            // 使用 RabbitTemplate 发送消息到指定的扇形交换器
            rabbitTemplate.convertAndSend(exchange, null, message);
            // 记录发送成功的日志
            log.info("发送消息成功==>{}", message.getClass().getSimpleName());
        } catch (AmqpException e) {
            // 记录发送失败的日志
            log.error("发送消息失败", e);
            // 抛出异常
            throw new AmqpException("发送失败");
        }
    }

    /**
     * 向指定的交换器和路由键发送消息。
     *
     * @param exchange 交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param message 发送的消息对象。
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
     * @param exchange 交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param messageSupplier 提供消息对象的 Lambda 表达式。
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
     * @param exchange 交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param messageSupplier 提供消息对象的 Lambda 表达式。
     * @param correlationData 关联数据，用于确认消息的唯一标识符等信息。
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
     * @param exchange 交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param messageSupplier 提供消息对象的 Lambda 表达式。
     * @param time 消息的延迟时间，单位为秒。
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
     * @param exchange 交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param messageSupplier 提供消息对象的 Lambda 表达式。
     * @param time 延迟时间。
     * @param timeUnit 延迟时间的时间单位。
     */
    public void sendDirect(String exchange, String routingKey,@Nullable Supplier<T> messageSupplier, Integer time, TimeType timeUnit) {
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
     * @param queue 队列名称，表示消息将被发送到的队列。
     * @param messageSupplier 提供消息对象的 Lambda 表达式。
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
            logIfEnabled(LogLevel.INFO, "发送消息成功===>{}", JSON.toJSONString(message));
        } catch (AmqpException e) {
            // 记录发送失败的日志
            logIfEnabled(LogLevel.ERROR, "发送消息失败===>{}", JSON.toJSONString(messageSupplier.get()), e);
            // 抛出异常
            throw new AmqpException("发送失败", e);
        }
    }

    /**
     * 使用 RabbitMQ 发送扇形（Fanout）消息，使用 Lambda 表达式延迟创建消息对象。
     * 扇形交换器会将接收到的消息广播到所有绑定到它的队列中。
     * 此方法负责将消息发送到指定的扇形交换器，不需要指定路由键。
     *
     * @param exchange 交换器名称，消息将被发送到这个交换器。
     * @param messageSupplier 提供消息对象的 Lambda 表达式。
     */
    public void sendFanout(String exchange,@Nullable Supplier<T> messageSupplier) {
        // 验证交换器是否有效
        validateExchange(exchange);
        try {
            // 获取消息对象
            T message = messageSupplier.get();
            // 使用 RabbitTemplate 发送消息到指定的扇形交换器
            rabbitTemplate.convertAndSend(exchange, null, message);
            // 记录发送成功的日志
            log.info("发送消息成功==>{}", message.getClass().getSimpleName());
        } catch (AmqpException e) {
            // 记录发送失败的日志
            log.error("发送消息失败", e);
            // 抛出异常
            throw new AmqpException("发送失败");
        }
    }

    /**
     * 向指定的交换器和路由键发送消息，使用 Lambda 表达式延迟创建消息对象。
     *
     * @param exchange 交换器名称，决定了消息将被发送到哪个交换器。
     * @param routingKey 路由键，与交换器类型一起决定消息将被路由到哪个队列。
     * @param messageSupplier 提供消息对象的 Lambda 表达式。
     */
    public void sendTopic(String exchange, String routingKey,@Nullable Supplier<T> messageSupplier) {
        // 验证交换器和路由键是否有效
        validateExchangeAndRoutingKey(exchange, routingKey);
        // 获取消息对象
        T message = messageSupplier.get();
        // 调用内部发送方法
        send(exchange, routingKey, message, null, null, null);
    }

    /**
     * 内部方法，用于发送消息。
     *
     * @param exchange 交换器名称。
     * @param routingKey 路由键。
     * @param message 消息对象。
     * @param correlationData 关联数据。
     * @param delayTime 延迟时间。
     * @param timeType 时间单位。
     */
    private void send(String exchange, String routingKey, @Nullable T message, CorrelationData correlationData, Integer delayTime, TimeType timeType) {
        try {
            // 创建消息后处理器，用于设置消息的延迟时间
            MessagePostProcessor processor = message1 -> {
                if (delayTime != null) {
                    long delay = timeType.toMilliseconds(delayTime);
                    message1.getMessageProperties().setDelayLong(delay);
                }
                return message1;
            };

            // 使用 RabbitTemplate 发送消息，并应用消息后处理器
            rabbitTemplate.convertAndSend(exchange, routingKey, message, processor, correlationData);
            // 记录发送成功的日志
            logIfEnabled(LogLevel.INFO, "发送消息成功 [exchange={}, routingKey={}, message={}]", exchange, routingKey, getMaskedMessage(message));
        } catch (AmqpConnectException e) {
            // 记录连接失败的日志
            logIfEnabled(LogLevel.ERROR, "连接RabbitMQ失败==>{}", JSON.toJSONString(message), e);
            // 抛出运行时异常
            throw new RuntimeException("连接失败", e);
        } catch (AmqpException e) {
            // 记录发送失败的日志
            logIfEnabled(LogLevel.ERROR, "发送消息失败 [exchange={}, routingKey={}, message={}] - {} : {}", exchange, routingKey, getMaskedMessage(message), e.getClass().getName(), e.getMessage());
            // 抛出 AMQP 异常
            throw new AmqpException("发送失败", e);
        }
    }

    /**
     * 验证交换器是否有效。
     *
     * @param exchange 交换器名称。
     */
    private void validateExchange(String exchange) {
        if (exchange == null || exchange.isEmpty()) {
            throw new IllegalArgumentException("无效的参数: exchange 不能为空");
        }
    }

    /**
     * 验证路由键是否有效。
     *
     * @param routingKey 路由键。
     */
    private void validateRoutingKey(String routingKey) {
        if (routingKey == null || routingKey.isEmpty()) {
            throw new IllegalArgumentException("无效的参数: routingKey 不能为空");
        }
    }

    /**
     * 验证交换器和路由键是否有效。
     *
     * @param exchange 交换器名称。
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
        if (queue == null || queue.isEmpty()) {
            throw new IllegalArgumentException("无效的参数: queue 不能为空");
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
     * @param exchange 交换器名称。
     * @param routingKey 路由键。
     * @param message 消息对象。
     * @param errorMessage 错误信息。
     * @param e 异常对象。
     */
    private void logFailure(String exchange, String routingKey, T message, String errorMessage, Exception e) {
        if (routingKey != null) {
            logIfEnabled(LogLevel.ERROR, errorMessage + " [exchange={}, routingKey={}, message={}]", exchange, routingKey, getMaskedMessage(message), e);
        } else {
            logIfEnabled(LogLevel.ERROR, errorMessage + " [destination={}, message={}]", exchange, getMaskedMessage(message), e);
        }
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
     * 根据日志级别记录日志。
     *
     * @param level 日志级别。
     * @param format 格式化的日志消息。
     * @param arguments 日志消息中的参数。
     */
    private void logIfEnabled(LogLevel level, String format, Object... arguments) {
        switch (level) {
            case INFO:
                if (log.isInfoEnabled()) {
                    log.info(format, arguments);
                }
                break;
            case DEBUG:
                if (log.isDebugEnabled()) {
                    log.debug(format, arguments);
                }
                break;
            case ERROR:
                if (log.isErrorEnabled()) {
                    log.error(format, arguments);
                }
                break;
            default:
                throw new IllegalArgumentException("不支持的日志级别: " + level);
        }
    }

}