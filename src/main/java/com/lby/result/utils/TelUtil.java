package com.lby.result.utils;

/**
 * TelUtil 类是一个用于生成模拟电话号码的工具类。
 */
public class TelUtil {

    // 模拟电话号码的前缀数组
    private static String[] telFirst="134,135,136,137,138,139,150,151,152,157,158,159,130,131,132,155,156,133,153".split(",");

    /**
     * 生成指定范围内的随机数。
     *
     * @param start 范围的起始值。
     * @param end 范围的结束值。
     * @return 生成的随机数。
     */
    public static int getNum(int start, int end) {
        return (int)(Math.random() * (end - start + 1) + start);
    }

    /**
     * 生成一个模拟的固定电话号码。
     *
     * @return 生成的模拟固定电话号码字符串。
     */
    public static String getLandline() {
        // 获取电话号码前缀的随机索引
        int index = getNum(0, telFirst.length - 1);
        // 获取电话号码前缀
        String first = telFirst[index];
        // 生成电话号码的第二部分
        String second = String.valueOf(getNum(1, 888) + 10000).substring(1);
        // 生成电话号码的第三部分
        String third = String.valueOf(getNum(1, 9100) + 10000).substring(1);
        // 拼接完整的电话号码
        return first + second + third;
    }
}

