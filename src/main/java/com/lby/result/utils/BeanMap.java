package com.lby.result.utils;

import com.lby.result.exception.CommonException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Supplier;

/**
 * 基于Spring BeanUtils的扩展，提供对象转换的工具方法。
 * 该类继承自BeanUtils，添加了针对单个对象和对象列表转换的功能。
 */
public class BeanMap extends BeanUtils {

    /**
     * 日志记录器。
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(BeanMap.class);


    /**
     * 将源对象转换为目标对象。
     * 此方法使用泛型来提高代码的复用性，并允许在转换过程中进行自定义异常处理。
     *
     * @param <S> 源对象的类型。
     * @param <T> 目标对象的类型。
     * @param source 源对象实例，必须有效且不可为空。
     * @param targetSupplier 一个 Supplier 函数式接口实例，用于创建目标对象，必须有效且不可为空。
     * @return 转换后的目标对象实例。
     * @throws IllegalArgumentException 如果源对象或目标对象供应商为 null，则抛出此异常。
     * @throws CommonException 如果转换过程中发生异常，则抛出此自定义异常。
     *
     * <pre>{@code
     * // 正常使用示例：将User对象转换为UserDTO对象
     * User user = new User("John", "Doe");
     * UserDTO userDTO = BeanMap.convertTo(user, UserDTO::new);
     *
     * // 参数校验失败示例（源对象为空）
     * try {
     *     BeanMap.convertTo(null, UserDTO::new);
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"Source 和 targetSupplier 不能为空"
     * }
     *
     * // 参数校验失败示例（目标对象供应商为空）
     * try {
     *     BeanMap.convertTo(user, null);
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"Source 和 targetSupplier 不能为空"
     * }
     * }</pre>
     */
   public static <S, T> T convertTo(S source, Supplier<T> targetSupplier) {
       // 检查源对象和目标对象供应商是否为空，如果任一为空，则抛出非法参数异常
       if (source == null || targetSupplier == null) {
           throw new IllegalArgumentException("Source 和 targetSupplier 不能为空");
       }
       try {
           // 尝试将源对象转换为目标对象
           return convertTo(source, targetSupplier, null);
       } catch (Exception e) {
           // 如果转换过程中发生异常，记录日志或抛出自定义异常
           throw new CommonException("转换对象错误");
       }
   }

    /**
     * 将源对象转换为目标对象，并通过回调函数进行额外处理。
     * 此方法使用泛型来提高代码的复用性，并允许在转换过程中进行自定义异常处理。
     *
     * @param <S> 源对象的类型。
     * @param <T> 目标对象的类型。
     * @param source 源对象实例，必须有效且不可为空。
     * @param targetSupplier 一个 Supplier 函数式接口实例，用于创建目标对象，必须有效且不可为空。
     * @param callBack 转换后的回调函数，用于执行额外的处理逻辑。如果不需要额外处理，可以传入 null。
     * @return 转换后的目标对象实例。如果转换过程中发生异常，则返回 null。
     * @throws IllegalArgumentException 如果源对象或目标对象供应商为 null，则抛出此异常。
     *
     * <pre>{@code
     * // 正常使用示例：将User对象转换为UserDTO对象，并执行回调函数
     * User user = new User("John", "Doe");
     * UserDTO userDTO = BeanMap.convertTo(user, UserDTO::new, (src, tgt) -> tgt.setFullName(src.getFirstName() + " " + src.getLastName()));
     *
     * // 回调函数为空的场景
     * UserDTO userDTOWithoutCallback = BeanMap.convertTo(user, UserDTO::new, null);
     *
     * // 参数校验失败示例（源对象为空）
     * try {
     *     BeanMap.convertTo(null, UserDTO::new, (src, tgt) -> tgt.setFullName(src.getFirstName() + " " + src.getLastName()));
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"源或目标为空"
     * }
     *
     * // 参数校验失败示例（目标对象供应商为空）
     * try {
     *     BeanMap.convertTo(user, null, (src, tgt) -> tgt.setFullName(src.getFirstName() + " " + src.getLastName()));
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"源或目标为空"
     * }
     * }</pre>
     */
    public static <S, T> T convertTo(S source, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack) {
        if (source == null || targetSupplier == null) {
            throw new IllegalArgumentException("源或目标为空");
        }

        T target = targetSupplier.get();

        try {
            copyProperties(source, target);
        } catch (Exception e) {
            LOGGER.error("复制属性错误: {}", e.getMessage(), e);
            return null;
        }

        if (callBack != null) {
            try {
                callBack.callBack(source, target);
            } catch (Exception e) {
                LOGGER.error("回调错误: {}", e.getMessage(), e);
            }
        }

        return target;
    }


    /**
     * 将源对象列表转换为目标对象列表。
     * 此方法使用泛型来提高代码的复用性，并允许在转换过程中进行自定义异常处理。
     *
     * @param <S> 源对象的类型。
     * @param <T> 目标对象的类型。
     * @param sources 源对象列表，必须有效且不可为空。
     * @param targetSupplier 一个 Supplier 函数式接口实例，用于创建目标对象，必须有效且不可为空。
     * @return 转换后的目标对象列表。
     * @throws CommonException 如果源列表或目标对象供应商为 null，则抛出此自定义异常。
     *
     * <pre>{@code
     * // 正常使用示例：将User对象列表转换为UserDTO对象列表
     * List<User> users = Arrays.asList(new User("John", "Doe"), new User("Jane", "Smith"));
     * List<UserDTO> userDTOs = BeanMap.convertListTo(users, UserDTO::new);
     *
     * // 参数校验失败示例（源列表为空）
     * try {
     *     BeanMap.convertListTo(null, UserDTO::new);
     * } catch (CommonException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"源列表不能为空"
     * }
     *
     * // 参数校验失败示例（目标对象供应商为空）
     * try {
     *     BeanMap.convertListTo(users, null);
     * } catch (CommonException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"目标不能为空"
     * }
     * }</pre>
     */
    public static <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier) {
        // 检查源列表是否为 null，如果是则抛出异常
        if (sources == null) {
            throw new CommonException("源列表不能为空");
        }
        // 检查目标供应商是否为 null，如果是则抛出异常
        if (targetSupplier == null) {
            throw new CommonException("目标不能为空");
        }
        // 调用重载方法，传入源列表和目标；转换器函数为 null
        return convertListTo(sources, targetSupplier, null);
    }

    /**
     * 将源对象列表转换为目标对象列表，并通过回调函数进行额外处理。
     * 此方法使用泛型来提高代码的复用性，并允许在转换过程中进行自定义异常处理。
     *
     * @param <S> 源对象的类型。
     * @param <T> 目标对象的类型。
     * @param sources 源对象列表，必须有效且不可为空。
     * @param targetSupplier 一个 Supplier 函数式接口实例，用于创建目标对象，必须有效且不可为空。
     * @param callBack 转换过程中的回调接口，用于执行额外操作。如果不需要额外处理，可以传入 null。
     * @return 转换后的目标对象列表。如果转换过程中发生异常，则返回空列表。
     * @throws CommonException 如果源列表或目标对象供应商为 null，则抛出此自定义异常。
     *
     * <pre>{@code
     * // 正常使用示例：将User对象列表转换为UserDTO对象列表，并执行回调函数
     * List<User> users = Arrays.asList(new User("John", "Doe"), new User("Jane", "Smith"));
     * List<UserDTO> userDTOs = BeanMap.convertListTo(users, UserDTO::new, (src, tgt) -> tgt.setFullName(src.getFirstName() + " " + src.getLastName()));
     *
     * // 回调函数为空的场景
     * List<UserDTO> userDTOsWithoutCallback = BeanMap.convertListTo(users, UserDTO::new, null);
     *
     * // 参数校验失败示例（源列表为空）
     * try {
     *     BeanMap.convertListTo(null, UserDTO::new, (src, tgt) -> tgt.setFullName(src.getFirstName() + " " + src.getLastName()));
     * } catch (CommonException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"源列表不能为空"
     * }
     *
     * // 参数校验失败示例（目标对象供应商为空）
     * try {
     *     BeanMap.convertListTo(users, null, (src, tgt) -> tgt.setFullName(src.getFirstName() + " " + src.getLastName()));
     * } catch (CommonException e) {
     *     System.out.println(e.getMessage());
     *     // 输出"目标不能为空"
     * }
     * }</pre>
     */
    public static <S, T> List<T> convertListTo(List<S> sources, Supplier<T> targetSupplier, ConvertCallBack<S, T> callBack)  {
        // 检查输入参数是否为空
        if (sources == null || targetSupplier == null) {
            return Collections.emptyList(); // 返回空列表而不是 null
        }

        // 创建目标列表
        List<T> list = Collections.synchronizedList(new ArrayList<>(sources.size()));

        // 遍历源列表
        for (S source : sources) {
            try {
                // 创建目标对象实例
                T target = targetSupplier.get();
                // 复制属性从源对象到目标对象
                copyProperties(source, target);
                // 如果回调接口不为空，则执行回调方法
                if (callBack != null) {
                    callBack.callBack(source, target);
                }
                // 将目标对象添加到目标列表中
                list.add(target);
            } catch (Exception e) {
                // 记录异常信息
               LOGGER.error("错误转换元素: " + e.getMessage());
               break;
            }
        }
        // 返回目标列表
        return list;
    }

    /**
     * 转换回调接口，用于在对象转换过程中执行额外的逻辑。
     *
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     * 回调接口
     * @param <S> 源对象类型
     * @param <T> 目标对象类型
     */
    @FunctionalInterface
    public interface ConvertCallBack<S, T> {
        /**
         * 执行转换回调。
         *
         * @param source 源对象
         * @param target 目标对象
         */
        void callBack(S t, T s);
    }

}
