package com.lby.result.utils;

import java.util.Random;

/**
 * BankCardGenerator 类用于生成银行卡片信息。
 * 该类提供了与银行卡片生成相关的方法，例如创建卡号、生成安全码等。
 */
public class BankCardGenerator {

    // 定义银行卡号前缀，这里以常见的银联卡62开头为例
    private static final String CARD_PREFIX = "62";
    // 定义银行卡号长度
    private static final int CARD_LENGTH = 16;
    // 随机数生成器
    private static final Random random = new Random();

    /**
     * 生成随机银行卡号
     * @return 生成的随机银行卡号字符串
     */
    public static String generateBankCardNumber() {
        //随机生成2位数字，从10开始
        int firstTwoDigits = 10 + random.nextInt(90);
        StringBuilder cardNumber = new StringBuilder(firstTwoDigits);
        String s = cardNumber.toString();
        // 生成剩余的数字部分
        for (int i = s.length(); i < CARD_LENGTH - 1; i++) {
            cardNumber.append(random.nextInt(10));
        }

        // 计算校验码
        int checksum = calculateChecksum(cardNumber.toString());
        cardNumber.append(checksum);

        return cardNumber.toString();
    }

    /**
     * 计算银行卡号的校验码
     * @param cardNumber 不包含校验码的银行卡号
     * @return 校验码
     */
    private static int calculateChecksum(String cardNumber) {
        int sum = 0;
        boolean isSecond = false;

        // 从右向左处理每一位数字
        for (int i = cardNumber.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(cardNumber.charAt(i));

            if (isSecond) {
                digit *= 2;
                if (digit > 9) {
                    digit -= 9;
                }
            }

            sum += digit;
            isSecond = !isSecond;
        }

        // 计算校验码
        int checksum = (10 - (sum % 10)) % 10;
        return checksum;
    }
}