package com.lby.result.utils;

/**
 * EmailUtil 类用于生成随机电子邮件地址
 */
public class EmailUtil {
    // 基础字符集，用于生成电子邮件地址的本地部分
    public static String base = "abcdefghijklmnopqrstuvwxyz0123456789";
    // 电子邮件后缀数组，包含一系列常用的电子邮件域名
    public static final String[] email_suffix="@gmail.com,@yahoo.com,@msn.com,@hotmail.com,@aol.com,@ask.com,@live.com,@qq.com,@0355.net,@163.com,@163.net,@263.net,@3721.net,@yeah.net,@googlemail.com,@126.com,@sina.com,@sohu.com,@yahoo.com.cn".split(",");

    /**
     * 生成指定范围内的随机整数
     *
     * @param start 随机数的最小值（包含）
     * @param end 随机数的最大值（包含）
     * @return start 和 end 之间的一个随机整数
     */
    public static int getNum(int start,int end) {
        return (int)(Math.random()*(end-start+1)+start);
    }

    /**
     * 生成指定长度范围内的随机电子邮件地址
     *
     * @param lMin 电子邮件地址本地部分的最小长度
     * @param lMax 电子邮件地址本地部分的最大长度
     * @return 生成的随机电子邮件地址
     */
    public static String getEmail(int lMin,int lMax) {
        // 随机决定电子邮件地址本地部分的长度
        int length=getNum(lMin,lMax);
        StringBuffer sb = new StringBuffer();
        // 生成电子邮件地址的本地部分
        for (int i = 0; i < length; i++) {
            int number = (int)(Math.random()*base.length());
            sb.append(base.charAt(number));
        }
        // 随机添加电子邮件后缀
        sb.append(email_suffix[(int)(Math.random()*email_suffix.length)]);
        return sb.toString();
    }
}
