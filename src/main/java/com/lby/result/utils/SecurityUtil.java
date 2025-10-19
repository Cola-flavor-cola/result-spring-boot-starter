package com.lby.result.utils;

import cn.hutool.core.codec.*;
import cn.hutool.core.codec.Base64;
import cn.hutool.core.util.HashUtil;
import cn.hutool.crypto.SecureUtil;
import com.lby.result.exception.CommonException;
import org.springframework.lang.Nullable;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;
import java.util.function.Function;


public class SecurityUtil {

    // 创建一个ForkJoinPool，用于执行并行操作
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    // 使用 ConcurrentHashMap 存储可并行操作的函数式接口实例，键为操作类型，值为对应的处理函数
    private static final ConcurrentHashMap<Integer, Function<String, String>> CONCURRENTHASHMAP = new ConcurrentHashMap<>();

    // 静态 Map 初始化，用于存储操作类型与处理函数的映射关系，以便在处理输入时根据类型快速找到对应的处理函数
    private static final Map<Integer, Function<String, String>> OPERATIONS = new HashMap<>();

    // 使用 SecureRandom 生成安全的随机数，主要用于加密或安全相关的操作，确保随机性足够高
    private static final SecureRandom RANDOM = new SecureRandom();


    /**
     * 这个静态代码块用于初始化操作类型与对应的处理函数映射。
     * 通过调用initializeMap方法，分别初始化CONCURRENTHASHMAP和OPERATIONS两个映射。
     **/
    static {
        initializeMap(CONCURRENTHASHMAP);
        initializeMap(OPERATIONS);
    }

    /**
     * 初始化编码算法映射表。
     *
     * @param map 需要初始化的映射表，键为算法标识，值为对应的编码函数。
     */
    private static void initializeMap(Map<Integer, Function<String, String>> map) {
        /* SHA-256 哈希算法 */
        map.put(0, t -> {
            try {
                return SecurityUtil.sha256(t);
            } catch (Exception e) {
                throw new CommonException("sha256编码错误", e);
            }
        });
        /* Base64 编码 */
        map.put(1, t -> {
            try {
                return SecurityUtil.base64Encode(t);
            } catch (Exception e) {
                throw new CommonException("base64编码错误", e);
            }
        });
        /* JS 哈希算法 */
        map.put(2, t -> {
            try {
                int hash = HashUtil.jsHash(t);
                return Integer.toString(hash);
            } catch (Exception e) {
                throw new CommonException("jsHash编码错误", e);
            }
        });
        /* 混合哈希算法 */
        map.put(3, t -> {
            try {
                long hash = HashUtil.mixHash(t);
                return Long.toString(hash);
            } catch (Exception e) {
                throw new CommonException("mixHash编码错误", e);
            }
        });
        /* Punycode 编码 */
        map.put(4, t -> {
            try {
                return PunyCode.encode(t);
            } catch (Exception e) {
                throw new CommonException("PunyCode编码错误", e);
            }
        });
        /* Rot13 编码 */
        map.put(5, t -> {
            try {
                return Rot.encode13(t);
            } catch (Exception e) {
                throw new CommonException("Rot13编码错误", e);
            }
        });
        /* MD5算法（注意：MD5已不安全，仅用于兼容性场景） */
        map.put(6, t -> {
            try {
                return SecureUtil.md5(t);
            } catch (Exception e) {
                throw new CommonException("MD5编码错误", e);
            }
        });
        /* Base32 编码 */
        map.put(7, t -> {
            try {
                return SecurityUtil.base32Encode(t);
            } catch (Exception e) {
                throw new CommonException("base32编码错误", e);
            }
        });
        /* Base62 编码 */
        map.put(8, t -> {
            try {
                return Base62.encode(t);
            } catch (Exception e) {
                throw new CommonException("Base62编码错误", e);
            }
        });
        /* SHA-1哈希算法（注意：SHA-1已不安全，仅用于兼容性场景） */
        map.put(9, t -> {
            try {
                return SecureUtil.sha1(t);
            } catch (Exception e) {
                throw new CommonException("SHA-1哈希算法错误", e);
            }
        });
    }


    /**
     * {@code getRandomStringCode()}方法实现示例：
     * <pre>{@code
     * // 生成默认6位数字验证码
     * String code = SecurityUtil.getRandomStringCode();
     *
     * // 验证码示例输出：345216
     *
     * // 方法调用流程说明：
     * // 1. 调用重载方法{@code getRandomStringCode(6)}
     * // 2. 使用SecureRandom生成0-9数字
     * // 3. 通过StringBuilder拼接为字符串返回
     * }
     * </pre>
     *
     * @return 6位纯数字验证码字符串
     */
    public static String getRandomStringCode() {
        return getRandomStringCode(6);
    }


    /**
     * 生成指定长度的随机数字字符串
     * <pre>{@code
     * // 生成默认6位随机数字字符串
     * String code = SecurityUtil.getRandomStringCode(6); // 输出类似 "123456"
     *
     * // 生成10位随机数字
     * String longCode = SecurityUtil.getRandomStringCode(10);
     *
     * // 参数小于等于0时抛出异常
     * try {
     *     String invalidCode = SecurityUtil.getRandomStringCode(-5);
     * } catch (IllegalArgumentException e) {
     *     System.out.println(e.getMessage()); // 输出 "长度必须大于0"
     * }
     *
     * // 传入null参数会抛出空指针异常（需确保参数非空）
     * try {
     *     String nullCode = SecurityUtil.getRandomStringCode(null);
     * } catch (NullPointerException e) {
     *     System.out.println("参数不能为null");
     * }
     * }</pre>
     *
     * @param length 指定生成随机数字字符串的长度，不应小于1
     * @return 生成的随机数字字符串
     * @throws IllegalArgumentException 如果指定的长度小于等于0，则抛出此异常
     */
    public static String getRandomStringCode(@Nullable Integer length) {
        // 验证输入的长度是否合法
        if (length <= 0) {
            throw new IllegalArgumentException("长度必须大于0");
        }

        // 使用StringBuilder来动态构建随机数字字符串
        StringBuilder sb = new StringBuilder();

        // 循环指定次数，每次生成一个随机数字，并添加到StringBuilder中
        for (int i = 0; i < length; i++) {
            // 生成0到9的随机数
            int num = RANDOM.nextInt(10);
            // 将生成的随机数字追加到StringBuilder中
            sb.append(num);
        }

        // 将构建好的随机数字字符串转换为普通字符串并返回
        return sb.toString();
    }

    /**
     * 生成指定长度的随机数字字符串（默认6位）
     *
     * @param length 需大于0的长度参数
     * @return 随机数字字符串
     * @throws IllegalArgumentException 长度参数非法
     *
     *                                  <pre>{@code
     *                                                                                                                                     // 默认长度调用
     *                                                                                                                                     String code6 = SecurityUtil.getRandomStringCode(); // "123456"
     *
     *                                                                                                                                     // 自定义长度
     *                                                                                                                                     String code10 = SecurityUtil.getRandomStringCode(10); // "0987654321"
     *
     *                                                                                                                                     // 异常示例
     *                                                                                                                                     try {
     *                                                                                                                                         SecurityUtil.getRandomStringCode(-5);
     *                                                                                                                                     } catch (IllegalArgumentException e) {
     *                                                                                                                                         System.out.println(e.getMessage()); // 输出"长度必须大于0"
     *                                                                                                                                     }
     *                                                                                                                                     }</pre>
     */
    public static Integer getRandomIntCode() {
        return generateRandomIntCode(6);
    }

    /**
     * 生成指定长度的随机数字字符串（默认6位）
     *
     * @param length 需大于0的长度参数
     * @return 随机数字字符串
     * @throws IllegalArgumentException 长度参数非法
     *
     *                                  <pre>{@code
     *                                                                                                                                     // 默认长度调用
     *                                                                                                                                     String code6 = SecurityUtil.getRandomStringCode(); // "123456"
     *
     *                                                                                                                                     // 自定义长度
     *                                                                                                                                     String code10 = SecurityUtil.getRandomStringCode(10); // "0987654321"
     *
     *                                                                                                                                     // 异常示例
     *                                                                                                                                     try {
     *                                                                                                                                         SecurityUtil.getRandomStringCode(-5);
     *                                                                                                                                     } catch (IllegalArgumentException e) {
     *                                                                                                                                         System.out.println(e.getMessage()); // 输出"长度必须大于0"
     *                                                                                                                                     }
     *                                                                                                                                     }</pre>
     */
    public static Integer generateRandomIntCode(@Nullable Integer length) {
        // 验证输入的长度是否合法
        if (length <= 0 || length > 9) {
            throw new IllegalArgumentException("长度必须在1到9之间");
        }

        // 计算最大值和最小值
        int minValue = (int) Math.pow(10, length - 1);
        int maxValue = (int) Math.pow(10, length) - 1;

        // 生成指定范围内的随机数
        return minValue + RANDOM.nextInt(maxValue - minValue + 1);
    }

    /**
     * 动态加密方法，根据提供的密钥对明文进行加密
     *
     * @param plaintext 明文字符串（不可为null）
     * @param key       密钥字符串（仅接受数字字符，不可为null）
     * @return 加密后的字符串
     * @throws CommonException 参数校验失败或加密异常
     *
     *                         <pre>{@code
     *                                                                                                 // 加密示例
     *                                                                                                 String encrypted = SecurityUtil.dynamicEncrypt("Hello", "123");
     *
     *                                                                                                 // 异常处理
     *                                                                                                 try {
     *                                                                                                     SecurityUtil.dynamicEncrypt(null, "123");
     *                                                                                                 } catch (CommonException e) {
     *                                                                                                     System.out.println(e.getMessage()); // 输出"输入文本或加密代码不能为空"
     *                                                                                                 }
     *                                                                                                 }</pre>
     */
    public static String dynamicEncrypt(@Nullable String plaintext, @Nullable String key) {
        // 检查输入文本和加密代码是否为空
        if (plaintext == null || key == null) {
            throw new CommonException("输入文本或加密代码不能为空");
        }

        // 将加密代码字符串拆分成字符数组
        List<Integer> list = new ArrayList<>();
        for (char c : key.toCharArray()) {
            try {
                // 将字符转换为对应的数字，并添加到列表中
                int num = Character.getNumericValue(c);
                list.add(num);
            } catch (NumberFormatException e) {
                // 如果字符不是数字或无法转换为数字，抛出异常
                throw new CommonException("加密代码字符串包含非法字符");
            }
        }
        // 遍历数字列表，每个数字对应一个可能的操作
        for (int num : list) {
            try {
                // 检查当前数字是否对应一个操作
                if (OPERATIONS.containsKey(num)) {
                    // 获取与当前数字关联的操作函数
                    Function<String, String> operation = OPERATIONS.get(num);
                    // 确保操作函数和待处理文本都不为空
                    if (operation != null && plaintext != null) {
                        // 使用当前操作函数处理文本
                        plaintext = operation.apply(plaintext);
                    }
                }
            } catch (Exception e) {
                // 如果操作过程中发生异常，抛出通用异常表示加密失败
                throw new CommonException("加密失败");
            }
        }
        // 将局部变量设置为 null 以帮助垃圾回收

        // 返回最终加密后的文本
        return plaintext;
    }

    /**
     * 动态加密方法，根据提供的密钥对明文进行加密
     *
     * @param plaintext 明文字符串（不可为null）
     * @param key       密钥字符串（仅接受数字字符，不可为null）
     * @return 加密后的字符串
     * @throws CommonException 参数校验失败或加密异常
     *
     *                         <pre>{@code
     *                                                                                                 // 加密示例
     *                                                                                                 String encrypted = SecurityUtil.dynamicEncrypt("Hello", "123");
     *
     *                                                                                                 // 异常处理
     *                                                                                                 try {
     *                                                                                                     SecurityUtil.dynamicEncrypt(null, "123");
     *                                                                                                 } catch (CommonException e) {
     *                                                                                                     System.out.println(e.getMessage()); // 输出"输入文本或加密代码不能为空"
     *                                                                                                 }
     *                                                                                                 }</pre>
     */
    public static String dynamicEncrypt(@Nullable String plaintext, @Nullable Integer key) {
        // 检查明文是否为空，如果为空，则抛出异常
        if (plaintext == null) {
            throw new CommonException("输入文本不能为空");
        }
        // 将整数密钥转换为数字列表，以便逐位处理
        List<Integer> list = new ArrayList<>();
        while (key > 0) {
            int digit = key % 10;
            list.add(digit);
            key /= 10;
        }
        // 反转列表以保持正确的顺序，因为是从低位到高位处理密钥的
        Collections.reverse(list);
        // 遍历数字列表，每个数字对应一个可能的操作
        for (int num : list) {
            try {
                // 检查当前数字是否对应一个操作
                if (OPERATIONS.containsKey(num)) {
                    // 获取与当前数字关联的操作函数
                    Function<String, String> operation = OPERATIONS.get(num);
                    // 确保操作函数和待处理文本都不为空
                    if (operation != null && plaintext != null) {
                        // 使用当前操作函数处理文本
                        plaintext = operation.apply(plaintext);
                    }
                }
            } catch (Exception e) {
                // 如果操作过程中发生异常，抛出通用异常表示加密失败
                throw new CommonException("加密失败");
            }
        }
        // 返回最终加密后的文本
        return plaintext;
    }

    /**
     * 线程并发加密方法，使用ForkJoinPool并行执行加密操作
     *
     * @param plaintext 明文字符串（不可为null）
     * @param key       密钥字符串（仅接受数字字符，不可为null）
     * @return 并行加密后的字符串
     * @throws CommonException 参数校验失败或加密异常
     *
     *                         <pre>{@code
     *                                                                                                 // 并行加密示例
     *                                                                                                 String encrypted = SecurityUtil.threadConcurrentEncryption("Secret", "1234");
     *
     *                                                                                                 // 异常处理
     *                                                                                                 try {
     *                                                                                                     SecurityUtil.threadConcurrentEncryption("Data", "ABCD");
     *                                                                                                 } catch (CommonException e) {
     *                                                                                                     System.out.println(e.getMessage()); // 输出"加密代码字符串包含非法字符"
     *                                                                                                 }
     *                                                                                                 }</pre>
     */
    public static String threadConcurrentEncryption(@Nullable String plaintext, @Nullable String key) {
        // 检查输入文本和加密代码是否为空
        if (plaintext == null || key == null) {
            throw new CommonException("输入文本或加密代码不能为空");
        }

        // 将加密代码字符串转换为数字列表，以便后续处理
        List<Integer> list = new ArrayList<>();
        for (char c : key.toCharArray()) {
            try {
                int num = Character.getNumericValue(c);
                list.add(num);
            } catch (NumberFormatException e) {
                // 如果加密代码中包含非数字字符，则抛出异常
                throw new CommonException("加密代码字符串包含非法字符");
            }
        }

        // 使用 ForkJoinPool 进行并行处理
        int parallelism = Runtime.getRuntime().availableProcessors();
        ForkJoinPool forkJoinPool = new ForkJoinPool(parallelism);

        // 提交加密任务到 ForkJoinPool，等待结果
        String result = FORK_JOIN_POOL.invoke(new CipherTask(plaintext, list, CONCURRENTHASHMAP));

        // 将局部变量设置为 null 以帮助垃圾回收
        list = null;

        // 返回加密后的结果
        return result;
    }

    /**
     * 线程并发加密方法，使用ForkJoinPool并行执行加密操作
     *
     * @param plaintext 明文字符串（不可为null）
     * @param key       密钥字符串（仅接受数字字符，不可为null）
     * @return 并行加密后的字符串
     * @throws CommonException 参数校验失败或加密异常
     *
     *                         <pre>{@code
     *                                                                                                 // 并行加密示例
     *                                                                                                 String encrypted = SecurityUtil.threadConcurrentEncryption("Secret", "1234");
     *
     *                                                                                                 // 异常处理
     *                                                                                                 try {
     *                                                                                                     SecurityUtil.threadConcurrentEncryption("Data", "ABCD");
     *                                                                                                 } catch (CommonException e) {
     *                                                                                                     System.out.println(e.getMessage()); // 输出"加密代码字符串包含非法字符"
     *                                                                                                 }
     *                                                                                                 }</pre>
     */
    public static String threadConcurrentEncryption(@Nullable String plaintext, @Nullable Integer key) {
        // 检查输入的明文是否为空，如果为空则抛出异常
        if (plaintext == null) {
            throw new CommonException("输入文本不能为空");
        }

        // 将整数密钥转换为数字列表，以便后续处理
        List<Integer> list = new ArrayList<>();
        while (key > 0) {
            int digit = key % 10;
            list.add(digit);
            key /= 10;
        }

        // 反转列表以保持正确的顺序，因为是从低位到高位处理密钥的
        java.util.Collections.reverse(list);

        // 使用 ForkJoinPool 进行并行处理，提高加密处理的效率
        String result = FORK_JOIN_POOL.invoke(new CipherTask(plaintext, list, CONCURRENTHASHMAP));
        return result;
    }

    /**
     * 动态加密给定的明文字符串。
     * 该方法生成一个随机加密密钥，并使用该密钥对明文进行加密。
     *
     * @param plaintext 可能为null的明文字符串，如果为null，则抛出异常。
     * @return 返回一个包含加密密钥和加密后密码的SecureData对象。
     * @throws CommonException 如果输入的明文为空，则抛出此异常。
     *
     *                         <pre>{@code
     *                                                                                                 // 正常使用示例：动态加密明文字符串
     *                                                                                                 SecureData encryptedData = SecurityUtil.dynamicEncrypt("Hello, World!");
     *                                                                                                 System.out.println("加密后的密码: " + encryptedData.getEncryptedPassword());
     *                                                                                                 System.out.println("加密密钥: " + encryptedData.getEncryptionKey());
     *
     *                                                                                                 // 参数校验失败示例（明文为空）
     *                                                                                                 try {
     *                                                                                                     SecurityUtil.dynamicEncrypt(null);
     *                                                                                                 } catch (CommonException e) {
     *                                                                                                     System.out.println(e.getMessage());
     *                                                                                                     // 输出"输入文本不能为空"
     *                                                                                                 }
     *                                                                                                 }</pre>
     */
    public static SecureData dynamicEncrypt(@Nullable String plaintext) {

        // 检查明文是否为空，如果为空则抛出异常
        if (plaintext == null) {
            throw new CommonException("输入文本不能为空");
        }

        // 生成一个随机的加密密钥
        String encryptionKey = getRandomStringCode();

        // 使用生成的加密密钥对明文进行动态加密
        String encryptedPassword = dynamicEncrypt(plaintext, encryptionKey);

        // 创建并返回一个包含加密后密码和加密密钥的EncryptedData对象
        return new SecureData(encryptedPassword, encryptionKey);
    }

    /**
     * 使用自定义密钥长度动态加密明文。
     * 该方法生成一个指定长度的随机加密密钥，并使用该密钥对明文进行加密。
     *
     * @param plaintext    待加密的明文字符串，如果为null，则抛出异常。
     * @param numberLength 指定的密钥长度，如果为null，则使用默认长度。
     * @return 返回一个包含加密密钥和加密后密码的SecureData对象。
     * @throws CommonException 如果输入的明文为空，则抛出此异常。
     *
     *                         <pre>{@code
     *                                                                                                 // 正常使用示例：动态加密明文字符串，指定密钥长度为8
     *                                                                                                 SecureData encryptedData = SecurityUtil.dynamicEncryptWithCustomKeyLength("Hello, World!", 8);
     *                                                                                                 System.out.println("加密后的密码: " + encryptedData.getEncryptedPassword());
     *                                                                                                 System.out.println("加密密钥: " + encryptedData.getEncryptionKey());
     *
     *                                                                                                 // 参数校验失败示例（明文为空）
     *                                                                                                 try {
     *                                                                                                     SecurityUtil.dynamicEncryptWithCustomKeyLength(null, 8);
     *                                                                                                 } catch (CommonException e) {
     *                                                                                                     System.out.println(e.getMessage());
     *                                                                                                     // 输出"输入文本不能为空"
     *                                                                                                 }
     *
     *                                                                                                 // 使用默认密钥长度
     *                                                                                                 SecureData defaultKeyLengthData = SecurityUtil.dynamicEncryptWithCustomKeyLength("Hello, World!", null);
     *                                                                                                 System.out.println("加密后的密码: " + defaultKeyLengthData.getEncryptedPassword());
     *                                                                                                 System.out.println("加密密钥: " + defaultKeyLengthData.getEncryptionKey());
     *                                                                                                 }</pre>
     */
    public static SecureData dynamicEncryptWithCustomKeyLength(@Nullable String plaintext, @Nullable Integer numberLength) {

        // 检查输入的明文是否为空
        if (plaintext == null) {
            throw new CommonException("输入文本不能为空");
        }

        // 根据指定的数字长度生成一个随机密钥，如果未提供，则使用默认长度
        String encryptionKey = getRandomStringCode(numberLength);

        // 使用生成的密钥加密明文
        String encryptedPassword = dynamicEncrypt(plaintext, encryptionKey);

        // 返回一个包含加密密文和密钥的对象
        return new SecureData(encryptedPassword, encryptionKey);
    }

    /**
     * 使用多线程并发加密的方式对明文进行加密。
     * 该方法生成一个随机加密密钥，并使用该密钥对明文进行加密。
     *
     * @param plaintext 可能为null的明文字符串，如果为null，则抛出异常。
     * @return 返回一个包含加密密钥和加密后密码的SecureData对象。
     * @throws CommonException 如果输入的明文为空，则抛出此异常。
     *
     *                         <pre>{@code
     *                                                                                                 // 正常使用示例：多线程并发加密明文字符串
     *                                                                                                 SecureData encryptedData = SecurityUtil.threadConcurrentEncryption("Hello, World!");
     *                                                                                                 System.out.println("加密后的密码: " + encryptedData.getEncryptedPassword());
     *                                                                                                 System.out.println("加密密钥: " + encryptedData.getEncryptionKey());
     *
     *                                                                                                 // 参数校验失败示例（明文为空）
     *                                                                                                 try {
     *                                                                                                     SecurityUtil.threadConcurrentEncryption(null);
     *                                                                                                 } catch (CommonException e) {
     *                                                                                                     System.out.println(e.getMessage());
     *                                                                                                     // 输出"输入文本不能为空"
     *                                                                                                 }
     *                                                                                                 }</pre>
     */
    public static SecureData threadConcurrentEncryption(@Nullable String plaintext) {
        // 检查明文是否为空，如果为空，则抛出异常
        if (plaintext == null) {
            throw new CommonException("输入文本不能为空");
        }

        // 生成一个随机的加密密钥
        String encryptionKey = getRandomStringCode();

        // 使用生成的密钥对明文进行加密
        String encryptedPassword = threadConcurrentEncryption(plaintext, encryptionKey);

        // 返回包含加密密文和加密密钥的EncryptedData对象
        return new SecureData(encryptedPassword, encryptionKey);
    }

    /**
     * 使用自定义密钥长度对文本进行线程并发加密。
     * 该方法生成一个指定长度的随机加密密钥，并使用该密钥对明文进行加密。
     *
     * @param plaintext    待加密的明文字符串，如果为null，则抛出异常。
     * @param numberLength 指定的密钥长度，如果为null，则使用默认长度。
     * @return 返回一个包含加密密钥和加密后密码的SecureData对象。
     * @throws CommonException 如果输入的明文为空，则抛出此异常。
     *
     *                         <pre>{@code
     *                                                                                                 // 正常使用示例：多线程并发加密明文字符串，指定密钥长度为8
     *                                                                                                 SecureData encryptedData = SecurityUtil.threadConcurrentEncryptionWithCustomKeyLength("Hello, World!", 8);
     *                                                                                                 System.out.println("加密后的密码: " + encryptedData.getEncryptedPassword());
     *                                                                                                 System.out.println("加密密钥: " + encryptedData.getEncryptionKey());
     *
     *                                                                                                 // 参数校验失败示例（明文为空）
     *                                                                                                 try {
     *                                                                                                     SecurityUtil.threadConcurrentEncryptionWithCustomKeyLength(null, 8);
     *                                                                                                 } catch (CommonException e) {
     *                                                                                                     System.out.println(e.getMessage());
     *                                                                                                     // 输出"输入文本不能为空"
     *                                                                                                 }
     *
     *                                                                                                 // 使用默认密钥长度
     *                                                                                                 SecureData defaultKeyLengthData = SecurityUtil.threadConcurrentEncryptionWithCustomKeyLength("Hello, World!", null);
     *                                                                                                 System.out.println("加密后的密码: " + defaultKeyLengthData.getEncryptedPassword());
     *                                                                                                 System.out.println("加密密钥: " + defaultKeyLengthData.getEncryptionKey());
     *                                                                                                 }</pre>
     */
    public static SecureData threadConcurrentEncryptionWithCustomKeyLength(@Nullable String plaintext, @Nullable Integer numberLength) {

        // 检查输入的明文是否为空，如果为空则抛出异常
        if (plaintext == null) {
            throw new CommonException("输入文本不能为空");
        }

        // 生成加密密钥，如果指定了密钥长度，则生成对应长度的密钥；否则生成默认长度的密钥
        String encryptionKey = getRandomStringCode(numberLength);

        // 使用生成的密钥对明文进行加密
        String encryptedPassword = threadConcurrentEncryption(plaintext, encryptionKey);

        // 返回包含加密后数据和加密密钥的对象
        return new SecureData(encryptedPassword, encryptionKey);
    }

    /**
     * 验证密码是否匹配的常量时间比较方法
     *
     * @param userPassword   用户输入的密码（可为null）
     * @param storedPassword 数据库存储的密码（可为null）
     * @return 密码是否匹配的布尔值
     * @throws CommonException SHA-256算法初始化失败
     *
     *                         <pre>{@code
     *                                                                                                 // 正确验证
     *                                                                                                 boolean match = SecurityUtil.verifyPassword("pass123", "pass123");
     *
     *                                                                                                 // 处理空值
     *                                                                                                 boolean nullCheck = SecurityUtil.verifyPassword(null, "pass123"); // 返回false
     *                                                                                                 }</pre>
     */
    public static Boolean verifyPassword(@Nullable String userPassword, @Nullable String storedPassword) {
        // 检查是否为同一个对象
        if (userPassword == storedPassword) {
            return true;
        }

        // 检查是否任一密码为空
        if (userPassword == null || storedPassword == null) {
            return false;
        }

        // 检查密码长度是否相同
        if (userPassword.length() != storedPassword.length()) {
            return false;
        }

        // 使用 MessageDigest 进行常量时间字符串比较
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash1 = digest.digest(userPassword.getBytes(StandardCharsets.UTF_8));
            byte[] hash2 = digest.digest(storedPassword.getBytes(StandardCharsets.UTF_8));
            return MessageDigest.isEqual(hash1, hash2);
        } catch (NoSuchAlgorithmException e) {
            throw new CommonException("无法获取 MessageDigest 实例", e);
        }
    }

    /**
     * CipherTask类继承自RecursiveTask，用于并行执行密码学操作
     * 它将一个大的操作列表分割成小的子列表，并行处理每个子列表，然后合并结果
     */
    private static class CipherTask extends RecursiveTask<String> {
        private final String text;
        private final List<Integer> operationsList;
        private final Map<Integer, Function<String, String>> operationsMap;
        private final int start;
        private final int end;

        /**
         * 构造函数用于初始化CipherTask
         *
         * @param text           需要进行密码学操作的文本
         * @param operationsList 操作的列表，每个操作由一个整数标识
         * @param operationsMap  操作的映射，每个操作标识对应一个执行操作的函数
         */
        public CipherTask(String text, List<Integer> operationsList, Map<Integer, Function<String, String>> operationsMap) {
            this(text, operationsList, operationsMap, 0, operationsList.size());
        }

        /**
         * 私有构造函数，用于创建子任务
         *
         * @param text           需要进行密码学操作的文本
         * @param operationsList 操作的列表，每个操作由一个整数标识
         * @param operationsMap  操作的映射，每个操作标识对应一个执行操作的函数
         * @param start          子任务处理的操作列表的起始索引
         * @param end            子任务处理的操作列表的结束索引
         */
        private CipherTask(String text, List<Integer> operationsList, Map<Integer, Function<String, String>> operationsMap, int start, int end) {
            this.text = text;
            this.operationsList = operationsList;
            this.operationsMap = operationsMap;
            this.start = start;
            this.end = end;
        }

        /**
         * 主要的并行计算方法如果操作列表的长度不大于1，则直接执行操作；
         * 否则，将任务分割成两个子任务，并行执行，然后合并结果
         *
         * @return 处理后的文本
         */
        @Override
        protected String compute() {
            if (end - start <= 1) {
                int num = operationsList.get(start);
                Function<String, String> operation = operationsMap.get(num);
                if (operation != null) {
                    return operation.apply(text);
                }
                return text;
            }

            int mid = (start + end) / 2;
            CipherTask leftTask = new CipherTask(text, operationsList, operationsMap, start, mid);
            CipherTask rightTask = new CipherTask(text, operationsList, operationsMap, mid, end);

            leftTask.fork();
            String rightResult = rightTask.compute();
            String leftResult = leftTask.join();

            // 合并左右子任务的结果
            return applyOperations(leftResult, rightResult, operationsList.subList(mid, end), operationsMap);
        }

        /**
         * 顺序地将一系列操作应用于文本
         *
         * @param text           初始文本
         * @param rightResult    右侧子任务的结果
         * @param operationsList 操作列表
         * @param operationsMap  操作映射
         * @return 应用操作后的文本
         */
        private String applyOperations(String text, String rightResult, List<Integer> operationsList, Map<Integer, Function<String, String>> operationsMap) {
            for (int num : operationsList) {
                Function<String, String> operation = operationsMap.get(num);
                if (operation != null) {
                    text = operation.apply(text);
                }
            }
            return text;
        }
    }

    /**
     * 将输入的字符串进行Base64编码
     *
     * @param text 待编码的原始字符串
     * @return 编码后的Base64字符串
     */
    private static String base64Encode(String text) {
        // 使用Base64类的静态方法encode将字节数组编码为Base64字符串
        // StandardCharsets.UTF_8确保在不同平台上的一致性
        return Base64.encode(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 将输入的字符串使用Base32编码
     * Base32编码是一种将二进制数据转换为32个不同字符的编码方式，常用于URL和文件名中
     *
     * @param text 待编码的字符串
     * @return 编码后的字符串
     */
    private static String base32Encode(String text) {
        // 使用Base32编码器将字符串编码为Base32格式
        // 这里使用StandardCharsets.UTF_8指定字符集，以确保跨平台兼容性
        return Base32.encode(text.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * 使用SHA-256算法对文本进行加密
     * SHA-256是一种加密算法，常用于信息安全领域，提供较高的安全性
     * 此方法封装了第三方库的SHA-256加密实现，简化了加密操作的使用
     *
     * @param text 需要加密的原始文本
     * @return 加密后的文本如果加密过程中发生错误，返回null
     */
    private static String sha256(String text) {
        return SecureUtil.sha256(text);
    }

}