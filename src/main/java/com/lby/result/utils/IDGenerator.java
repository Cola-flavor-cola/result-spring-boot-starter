package com.lby.result.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * IDGenerator类用于生成唯一的ID
 * 该类的主要作用是提供一个机制，通过该机制可以获取到唯一的ID，通常用于数据库主键生成、唯一标识符等场景
 */
public class IDGenerator {


    private static final Random random = new Random();
    private static final ThreadLocal<SimpleDateFormat> dateFormatThreadLocal = ThreadLocal.withInitial(() -> new SimpleDateFormat("yyyyMMdd"));

    private static final String[] areaCodes = {"110000", "120000", "130000", "140000", "150000", "210000", "220000", "230000", "310000", "320000", "330000", "340000", "350000", "360000", "370000", "410000", "420000", "430000", "440000", "450000", "460000", "500000", "510000", "520000", "530000", "540000", "610000", "620000", "630000", "640000", "650000", "710000", "810000", "820000"};

    private static Date generateRandomBirthDate() {
        long start = System.currentTimeMillis() - 365L * 24 * 60 * 60 * 1000 * 100; // 100 years ago
        long end = System.currentTimeMillis();
        return new Date(ThreadLocalRandom.current().nextLong(start, end));
    }

    /**
     * 生成一个随机的身份证号码
     *
     * @return 随机生成的身份证号码字符串
     */
    public static String generateRandomID() {
        // 随机选择一个地区代码
        String areaCode = areaCodes[random.nextInt(areaCodes.length)];

        // 随机生成出生日期
        Date birthDate = generateRandomBirthDate();
        SimpleDateFormat dateFormat = dateFormatThreadLocal.get();
        String birthDateString = dateFormat.format(birthDate);

        // 随机生成顺序号（3位）
        String sequenceStr = String.format("%03d", random.nextInt(1000));

        // 拼接身份证号，不包括最后的校验码
        String idNumber = areaCode + birthDateString + sequenceStr;

        // 计算校验码
        int[] factors = {7, 9, 10, 5, 8, 4, 2, 1, 6, 3, 7, 9, 10, 5, 8, 4, 2};
        String[] checkCodes = {"1", "0", "X", "9", "8", "7", "6", "5", "4", "3", "2"};
        int sum = 0;
        // 对身份证号的前17位进行加权求和
        for (int i = 0; i < 17; i++) {
            sum += Character.getNumericValue(idNumber.charAt(i)) * factors[i];
        }
        // 计算模数
        int mod = sum % 11;
        // 根据模数获取校验码
        String checkCode = checkCodes[mod];

        // 返回完整的身份证号码，包括校验码
        return idNumber + checkCode;
    }
}