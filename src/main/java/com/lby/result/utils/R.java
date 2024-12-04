package com.lby.result.utils;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;

/**
 * 通用的响应类，用于封装API请求的结果
 * 该类泛型化，以便可以携带任何类型的响应数据
 * 实现Serializable接口，使得响应对象可以被序列化，便于在网络间传输或存储
 *
 * @param <T> 泛型参数，表示可以携带的响应数据的类型
 */
@Data
public class R<T> implements Serializable {

    /**
     * 状态码，用于标识响应的状态
     */
    private Integer code;

    /**
     * 消息，对响应状态的简短描述
     */
    private String msg;

    /**
     * 数据，响应中携带的具体数据部分
     * 由于数据类型可能根据具体响应内容而变化，因此使用泛型T来表示
     */
    private T data;

    // 生成与类版本相关的唯一标识符，用于序列化和反序列化过程，确保类的版本一致性和数据完整性
    private static final long serialVersionUID = -3960261604605958516L;

    // 定义成功操作的状态码常量，便于在结果比较和返回时使用
    private static final int SUCCESS_CODE = ResultCode.SUCCESS.getCode();

    // 定义错误操作的状态码常量，用于标识和处理错误情况
    private static final int ERROR_CODE = ResultCode.ERROR.getCode();

    // 静态初始化块，用于在类加载时初始化常量或静态成员
    static {
        // 设置请求模式不支持时的消息
        ResultCode.REQ_MODE_NOT_SUPPORTED.setMsg("请求方式不支持");
    }

    /**
     * 构造函数，用于初始化R对象
     * 此构造函数没有参数，调用了带有两个参数的构造函数
     * 参数 SUCCESS_CODE 表示成功状态码，null 表示没有初始数据
     * 这样的设计是为了提供一个简单的成功状态初始化方式
     */
    private R() {
        this(SUCCESS_CODE, null);
    }

    /**
     * 构造函数，用于创建一个带有数据的响应对象
     * 默认状态码为成功
     *
     * @param data 响应数据
     */
    private R(T data) {
        this(SUCCESS_CODE, null, data);
    }

    /**
     * 构造函数，用于创建一个带有消息的响应对象
     * 默认状态码为成功，无数据
     *
     * @param msg 响应消息
     */
    private R(String msg) {
        this(SUCCESS_CODE, msg, null);
    }

    /**
     * 构造函数，用于创建一个同时带有消息和数据的响应对象
     * 默认状态码为成功
     *
     * @param msg 响应消息
     * @param data 响应数据
     */
    private R(String msg, T data) {
        this(SUCCESS_CODE, msg, data);
    }

    /**
     * 构造函数，用于创建一个带有状态码、消息和数据的响应对象
     *
     * @param code 状态码
     * @param msg 响应消息
     * @param data 响应数据
     */
    private R(int code, String msg, T data) {
        this.code = code;
        this.msg = msg;
        this.data = data;
    }

    /**
     * 构造函数，用于创建一个带有状态码和消息的响应对象
     * 无响应数据
     *
     * @param code 状态码
     * @param msg 响应消息
     */
    private R(int code, String msg) {
        this(code, msg, null);
    }

    /**
     * 构造函数，用于从ResultCode枚举创建一个带有数据的响应对象
     *
     * @param resultCode 枚举类型的响应状态
     * @param data 响应数据
     */
    private R(ResultCode resultCode, T data) {
        this(resultCode.getCode(), resultCode.getMsg(), data);
    }

    /**
     * 构造函数，用于从ResultCode枚举创建一个响应对象
     * 无响应数据
     *
     * @param resultCode 枚举类型的响应状态
     */
    private R(ResultCode resultCode) {
        this(resultCode.getCode(), resultCode.getMsg(), null);
    }

    /**
     * 创建一个表示成功响应的R对象，不含数据
     *
     * @param <T> 泛型参数，表示可能返回的数据类型
     * @return R<T> 返回一个表示成功响应的R对象，成功代码为SUCCESS_CODE，数据为null
     */
    public static <T> R<T> success() {
        return new R<>(SUCCESS_CODE, null);
    }

    /**
     * 创建一个表示成功响应的对象，包含成功数据
     * 此方法用于快速构建一个成功响应的R对象，传入数据作为成功响应的附加信息
     *
     * @param <T> 泛型参数，表示数据的类型
     * @param data 成功响应的数据，可以是任意类型
     * @return 返回一个包含成功数据的R对象
     */
    public static <T> R<T> success(T data) {
        return new R<>(SUCCESS_CODE, null, data);
    }

    /**
     * 创建一个表示成功响应的操作结果对象
     * 该方法用于在操作成功时，返回一个不包含数据的响应对象
     * 主要用于不需要返回具体数据，只需告知调用者操作成功的情况
     *
     * @param msg 成功消息，用于描述成功情况或提供额外信息
     * @param <T> 泛型参数，表示可能的返回数据类型，此处为null
     * @return 返回一个表示成功响应的R对象，包含成功消息和null数据
     */
    public static <T> R<T> success(String msg) {
        return new R<>(SUCCESS_CODE, msg, null);
    }

    /**
     * 创建一个表示成功响应的对象，携带自定义消息和数据
     * 该方法用于当操作成功时，返回给客户端一个包含成功消息和特定数据的响应对象
     * 通过泛型，允许携带任何类型的对象作为数据
     *
     * @param msg 成功操作的描述信息，用于告知客户端操作的具体结果
     * @param data 操作成功后返回的数据，可以是任意类型
     * @return R<T> 一个泛型类R的实例，包含成功代码、消息和数据
     */
    public static <T> R<T> success(String msg, T data) {
        return new R<>(SUCCESS_CODE, msg, data);
    }

    /**
     * 创建一个表示错误响应的对象
     *
     * 此方法用于生成一个通用的错误响应对象，其中包含了错误代码和空的数据
     * 它是一个泛型方法，允许调用者指定响应数据的类型，尽管在此情况下响应数据为null
     *
     * @param <T> 响应数据的类型，由调用者指定
     * @return 返回一个包含错误代码和空数据的响应对象
     */
    public static <T> R<T> error() {
        return new R<>(ERROR_CODE, null);
    }

    /**
     * 创建一个表示错误响应的对象，携带特定数据
     * 该方法用于当需要包装一个错误响应，附带错误相关数据时使用
     * 通过返回一个R对象，将错误状态和数据解耦，以便于上层处理
     *
     * @param <T> 泛型参数，表示data参数的数据类型
     * @param data 错误相关的数据，可以是描述错误信息的对象或null
     * @return 返回一个R对象，包含错误码、空的消息和提供的错误数据
     */
    public static <T> R<T> error(T data) {
        return new R<>(ERROR_CODE, null, data);
    }

    /**
     * 创建一个表示错误响应的对象
     * <p>
     * 此方法用于生成一个泛型类R的实例，该实例表示一个错误响应它包含错误消息但没有数据体
     * 主要用于向调用者指示一个错误情况或操作失败
     *
     * @param msg 错误消息，提供关于错误的详细信息
     * @return 返回一个泛型类R的实例，包含错误代码、错误消息和空的数据体
     */
    public static <T> R<T> error(String msg) {
        return new R<>(ERROR_CODE, msg, null);
    }

    /**
     * 创建一个错误的响应对象，携带特定的消息和数据
     * 该方法用于当操作失败时，返回一个包含错误信息和可选数据的响应对象
     *
     * @param msg 错误消息，用于描述错误的详细信息
     * @param data 可选的数据对象，用于携带附加信息或数据
     * @param <T> 泛型参数，表示数据对象的类型
     * @return 返回一个错误的响应对象，包含错误代码、错误消息和可选的数据对象
     */
    public static <T> R<T> error(String msg, T data) {
        return new R<>(ERROR_CODE, msg, data);
    }

    /**
     * 创建一个表示错误响应的对象
     * <p>
     * 此方法用于生成一个带有错误信息的响应对象它允许调用者指定错误代码和消息，
     * 并统一返回一个格式化的响应对象，便于前端处理和展示错误信息
     *
     * @param code 错误代码，用于标识具体的错误类型
     * @param msg 错误消息，提供更详细的错误描述
     * @param <T> 响应对象中数据类型的泛型参数，由于错误响应不包含数据，因此此处为null
     * @return 返回一个带有错误信息的响应对象，其中的数据部分为null
     */
    public static <T> R<T> error(int code, String msg) {
        return new R<>(code, msg, null);
    }

    /**
     * 创建一个错误的响应对象，用于封装错误信息和数据
     *
     * @param <T> 泛型参数，表示可以传入任意类型的对象
     * @param code 错误代码，表示错误的类型
     * @param data 随错误返回的数据，可以是任意类型
     * @return 返回一个封装了错误代码、错误消息和数据的R对象
     */
    public static <T> R<T> error(int code, T data) {
        return new R<>(code, ResultCode.ERROR.getMsg(), data);
    }

    /**
     * 创建一个表示错误响应的对象
     * <p>
     * 此方法用于简化错误响应的创建过程它接受一个ResultCode枚举作为参数，
     * 该枚举定义了错误代码和消息，然后返回一个包含这些信息的R对象
     *
     * @param <T> 响应对象中数据类型的泛型参数
     * @param resultCode 表示错误代码和消息的枚举类型
     * @return 返回一个包含错误信息的响应对象
     */
    public static <T> R<T> error(ResultCode resultCode) {
        return new R<>(resultCode);
    }

    /**
     * 自定义序列化时的对象读取方法
     * 该方法在反序列化对象时被调用，用于确保反序列化后的对象状态合法
     *
     * @param in ObjectInputStream类型的输入流，用于读取序列化的对象
     * @throws IOException 如果在读取对象过程中发生I/O错误
     * @throws ClassNotFoundException 如果无法找到序列化对象对应的类
     * @throws InvalidObjectException 如果序列化后的对象状态不合法
     */
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
        in.defaultReadObject();
        // 检查序列化后的对象是否合法
        if (this.code < 0 || this.code > 999) {
            throw new InvalidObjectException("Invalid code value: " + this.code);
        }
    }
}