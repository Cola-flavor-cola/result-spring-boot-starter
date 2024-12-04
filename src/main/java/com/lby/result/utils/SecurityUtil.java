package com.lby.result.utils;

import cn.hutool.core.codec.Base64;
import cn.hutool.core.codec.*;
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

/**
 * 加密工具类，提供了一系列加密相关的方法
 * 该类主要用于对数据进行加密处理，提供了不同的加密算法和模式供选择
 */
public class SecurityUtil {

    // 创建一个ForkJoinPool，用于执行并行操作
    private static final ForkJoinPool FORK_JOIN_POOL = new ForkJoinPool(Runtime.getRuntime().availableProcessors());

    // 使用 ConcurrentHashMap 存储可并行操作的函数式接口实例，键为操作类型，值为对应的处理函数
    private static final ConcurrentHashMap<Integer, Function<String, String>> CONCURRENTHASHMAP = new ConcurrentHashMap<>();

    // 静态 Map 初始化，用于存储操作类型与处理函数的映射关系，以便在处理输入时根据类型快速找到对应的处理函数
    private static final Map<Integer, Function<String, String>> OPERATIONS = new HashMap<>();

    // 使用 SecureRandom 生成安全的随机数，主要用于加密或安全相关的操作，确保随机性足够高
    private static final SecureRandom RANDOM = new SecureRandom();

    // 初始化操作类型与处理函数的映射关系
    static {
        // SHA-256 哈希算法
        CONCURRENTHASHMAP.put(0, t -> {
            try {
                return SecurityUtil.sha256(t);
            } catch (Exception e) {
                throw new CommonException("sha256编码错误");
            }
        });
        // Base64 编码
        CONCURRENTHASHMAP.put(1, t -> {
            try {
                return SecurityUtil.base64Encode(t);
            } catch (Exception e) {
                throw new CommonException("base64编码错误");
            }
        });
        // JS 哈希算法
        CONCURRENTHASHMAP.put(2, t -> {
            try {
                int hash = HashUtil.jsHash(t);
                return Integer.toString(hash);
            } catch (Exception e) {
                throw new CommonException("jsHash编码错误");
            }
        });
        // 混合哈希算法
        CONCURRENTHASHMAP.put(3, t -> {
            try {
                long hash = HashUtil.mixHash(t);
                return Long.toString(hash);
            } catch (Exception e) {
                throw new CommonException("mixHash编码错误");
            }
        });
        // Punycode 编码
        CONCURRENTHASHMAP.put(4, t -> {
            try {
                return PunyCode.encode(t);
            } catch (Exception e) {
                throw new CommonException("PunyCode编码错误");
            }
        });
        // Rot13 编码
        CONCURRENTHASHMAP.put(5, t -> {
            try {
                return Rot.encode13(t);
            } catch (Exception e) {
                throw new CommonException("Rot13编码错误");
            }
        });
        // MD5算法
        CONCURRENTHASHMAP.put(6, t -> {
            try {
                return SecureUtil.md5(t);
            } catch (Exception e) {
                throw new CommonException("MD5编码错误");
            }
        });
        // Base32 编码
        CONCURRENTHASHMAP.put(7, t -> {
            try {
                return SecurityUtil.base32Encode(t);
            } catch (Exception e) {
                throw new CommonException("base32编码错误");
            }
        });
        // Base62 编码
        CONCURRENTHASHMAP.put(8, t -> {
            try {
                return Base62.encode(t);
            } catch (Exception e) {
                throw new CommonException("Base62编码错误");
            }
        });
        // SHA-1哈希算法
        CONCURRENTHASHMAP.put(9, t -> {
            try {
                return SecureUtil.sha1(t);
            } catch (Exception e) {
                throw new CommonException("SHA-1哈希算法错误");
            }
        });
    }

    /**
     * 生成随机字符串验证码
     * 默认生成6位长度的字符串验证码
     *
     * @return 生成的验证码字符串
     */
    public static String getRandomStringCode() {
        return getRandomStringCode(6);
    }

    /**
     * 生成指定长度的随机数字字符串
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
     * 获取随机的整数验证码
     * <p>
     * 该方法用于生成一个指定长度的随机整数验证码，用于用户注册、登录等场景
     * 验证码是用户进行身份验证的重要信息，因此需要保证其安全性和可靠性
     *
     * @return 随机生成的整数验证码
     */
    public static Integer getRandomIntCode() {
        return generateRandomIntCode(6);
    }

    /**
     * 生成指定长度的随机整数代码
     *
     * @param length 期望生成的随机整数的位数，必须在1到9之间
     * @return 生成的随机整数
     * @throws IllegalArgumentException 如果指定的长度不在1到9之间，则抛出此异常
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
     * @param plaintext 可能为空的明文字符串，如果为空，则抛出异常
     * @param key 可能为空的字符密钥，如果为空，则不执行加密操作
     * @return 加密后的字符串
     * @throws CommonException 如果输入文本为空或加密过程中发生错误，则抛出此异常
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
        list = null;
        // 返回最终加密后的文本
        return plaintext;
    }

    /**
     * 动态加密方法，根据提供的密钥对明文进行加密
     *
     * @param plaintext 可能为空的明文字符串，如果为空，则抛出异常
     * @param key 可能为空的密钥整数，如果为空，则不执行加密操作
     * @return 加密后的字符串
     * @throws CommonException 如果输入文本为空或加密过程中发生错误，则抛出此异常
     */
    public static String dynamicEncrypt(@Nullable String plaintext,@Nullable Integer key) {
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
     * 执行基于线程并发的加密操作
     * 该方法采用 ForkJoin 并行框架来处理加密任务，旨在通过多线程提高加密效率
     *
     * @param plaintext 待加密的明文字符串
     * @param key 加密密钥字符串，应仅包含数字
     * @return 加密后的字符串
     * @throws CommonException 如果输入文本或密钥为空，或密钥包含非数字字符，则抛出此异常
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
        String result = FORK_JOIN_POOL.invoke(new CipherTask(plaintext, list, OPERATIONS));

        // 将局部变量设置为 null 以帮助垃圾回收
        list = null;

        // 返回加密后的结果
        return result;
    }

    /**
     * 执行基于线程并发的加密操作
     * 该方法接收一个明文字符串和一个整数密钥，然后使用密钥对明文进行加密
     * 加密过程是并行执行的，以提高处理效率
     *
     * @param plaintext 可能为空的明文字符串，如果为空将抛出异常
     * @param key       可能为空的整数密钥，用于加密明文
     * @return 加密后的密文字符串
     * @throws CommonException 如果输入文本为空，则抛出此异常
     */
    public static String threadConcurrentEncryption(@Nullable String plaintext,@Nullable Integer key) {
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
        String result = FORK_JOIN_POOL.invoke(new CipherTask(plaintext, list, OPERATIONS));
        return result;
    }

    /**
     * 检查两个密码是否相等
     * 使用 SHA-256 哈希算法进行常量时间比较，以防止时间攻击
     *
     * @param userPassword 用户输入的密码
     * @param storedPassword 存储在数据库中的密码
     * @return 如果密码相等则返回 true，否则返回 false
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