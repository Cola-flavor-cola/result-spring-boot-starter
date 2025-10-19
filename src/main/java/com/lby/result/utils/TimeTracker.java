package com.lby.result.utils;

/**
 * 性能跟踪工具类，用于测量代码执行时间并提供灵活的异常处理机制。
 *
 * <p>主要特性：
 * <ul>
 *   <li>精确测量代码执行时间</li>
 *   <li>支持带返回值和无返回值的方法跟踪</li>
 *   <li>提供两种异常处理模式</li>
 *   <li>支持自动资源管理</li>
 * </ul>
 *
 * <h2>使用示例：</h2>
 *
 * <h3> try-with-resources 手动跟踪</h3>
 * <pre>{@code
 * // 手动管理资源和性能跟踪
 * try (TimeTracker tracker = new TimeTracker("数据库操作")) {
 *     database.connect();
 *     database.executeQuery();
 * } // 自动关闭，并打印执行时间
 *
 * // 带返回值的try-with-resources
 * try (TimeTracker tracker = new TimeTracker("复杂计算");
 *      Resource resource = acquireResource()) {
 *     return performComplexCalculation(resource);
 * }
 * }</pre>
 *
 * <h3>结合静态方法的try-with-resources</h3>
 * <pre>{@code
 * try (TimeTracker ignored = TimeTracker.of("网络请求")) {
 *     httpClient.sendRequest();
 *     httpClient.receiveResponse();
 * }
 * }</pre>
 *
 * <p>注意：使用try-with-resources可以确保资源正确关闭，
 * 并自动记录执行时间。</p>
 *
 * <h3>lambda自动处理异常</h3>
 * <pre>{@code
 * // 无返回值方法
 * TimeTracker.track("数据处理", () -> {
 *     processData(); // 可能抛出异常的方法
 * });
 *
 * // 有返回值方法
 * String result = TimeTracker.track("查询用户", () -> {
 *     return userService.findById(123);
 * });
 * }</pre>
 *
 * <h3>lambda显式异常处理</h3>
 * <pre>{@code
 * try {
 *     // 允许抛出原始异常
 *     String result = TimeTracker.trackThrows("复杂查询", () -> {
 *         return complexQuery(); // 可能抛出检查异常
 *     });
 * } catch (SQLException e) {
 *     // 精确处理特定异常
 *     logger.error("数据库查询失败", e);
 * }
 * }</pre>
 *
 * <h3>lambda嵌套使用</h3>
 * <pre>{@code
 * TimeTracker.track("整体流程", () -> {
 *     // 子任务1
 *     TimeTracker.track("数据准备", () -> prepareData());
 *
 *     // 子任务2
 *     return TimeTracker.track("数据处理", () -> processData());
 * });
 * }</pre>
 *
 * <p>注意：默认情况下会打印执行时间到控制台。对于生产环境，
 * 建议根据需要自定义日志记录机制。</p>
 *
 * @author [lby]
 * @version 1.0
 * @since [1.0.0]
 */
public class TimeTracker implements AutoCloseable {
    /**
     * 操作名称
     */
    private String operationName;
    /**
     * 开始时间（纳秒）
     */
    private long startTime;
    /**
     * 是否启用日志
     */
    private boolean logEnabled;

    /**
     * 创建一个新的TimeTracker实例。
     *
     * @param operationName 要跟踪的操作名称
     */
    public TimeTracker(String operationName) {
        this(operationName, true);
    }

    /**
     * 私有构造函数，用于创建TimeTracker实例。
     *
     * @param operationName 操作名称
     * @param logEnabled    是否启用日志输出
     */
    private TimeTracker(String operationName, boolean logEnabled) {
        this.operationName = operationName;
        this.startTime = System.nanoTime();
        this.logEnabled = logEnabled;
        if (logEnabled) {
            System.out.printf("开始执行: %s%n", operationName);
        }
    }

    /**
     * 创建一个新的TimeTracker实例的静态工厂方法。
     *
     * @param operationName 要跟踪的操作名称
     * @return 新的TimeTracker实例
     */
    public static TimeTracker of(String operationName) {
        return new TimeTracker(operationName);
    }

    /**
     * 跟踪带返回值的代码块执行时间，异常会被包装为RuntimeException。
     *
     * @param operationName 操作名称
     * @param execution     要执行的代码块
     * @param <T>           返回值类型
     * @return 代码块的执行结果
     * @throws RuntimeException 如果执行过程中发生异常
     */
    public static <T> T track(String operationName, ThrowableSupplier<T> execution) {
        try {
            return trackThrows(operationName, execution);
        } catch (Exception e) {
            throw new RuntimeException("执行失败: " + operationName, e);
        }
    }

    /**
     * 跟踪带返回值的代码块执行时间，允许抛出异常。
     *
     * @param operationName 操作名称
     * @param execution     要执行的代码块
     * @param <T>           返回值类型
     * @return 代码块的执行结果
     * @throws Exception 如果执行过程中发生异常
     */
    public static <T> T trackThrows(String operationName, ThrowableSupplier<T> execution) throws Exception {
        try (TimeTracker ignored = new TimeTracker(operationName, true)) {
            return execution.get();
        }
    }

    /**
     * 跟踪无返回值的代码块执行时间，异常会被包装为RuntimeException。
     *
     * @param operationName 操作名称
     * @param execution     要执行的代码块
     * @throws RuntimeException 如果执行过程中发生异常
     */
    public static void track(String operationName, ThrowableRunnable execution) {
        try {
            trackThrows(operationName, execution);
        } catch (Exception e) {
            throw new RuntimeException("执行失败: " + operationName, e);
        }
    }

    /**
     * 跟踪无返回值的代码块执行时间，允许抛出异常。
     *
     * @param operationName 操作名称
     * @param execution     要执行的代码块
     * @throws Exception 如果执行过程中发生异常
     */
    public static void trackThrows(String operationName, ThrowableRunnable execution) throws Exception {
        try (TimeTracker ignored = new TimeTracker(operationName, true)) {
            execution.run();
        }
    }

    /**
     * 关闭资源并记录操作执行时间。
     * <p>
     * 当日志功能启用时（{@code logEnabled} 为 {@code true}），计算操作从开始到关闭的耗时（以毫秒为单位），并打印包含操作名称和耗时的日志信息。
     */
    @Override
    public void close() {
        // 如果启用日志记录，则记录操作执行时间
        if (logEnabled) {
            // 计算执行时间（转换为毫秒）
            long timeElapsed = (System.nanoTime() - startTime) / 1_000_000;
            System.out.printf("%s 执行完成，耗时: %d ms%n", operationName, timeElapsed);
        }
    }

    /**
     * 可抛出异常的Supplier函数式接口。
     *
     * @param <T> 返回值类型
     */
    @FunctionalInterface
    public interface ThrowableSupplier<T> {
        /**
         * 获取结果。
         *
         * @return 执行结果
         * @throws Exception 如果执行过程中发生错误
         */
        T get() throws Exception;
    }

    /**
     * 可抛出异常的Runnable函数式接口。
     */
    @FunctionalInterface
    public interface ThrowableRunnable {
        /**
         * 执行操作。
         *
         * @throws Exception 如果执行过程中发生错误
         */
        void run() throws Exception;
    }
}