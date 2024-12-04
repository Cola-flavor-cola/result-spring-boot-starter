package com.lby.result.utils;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.date.BetweenFormatter;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.thread.ExecutorBuilder;

import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * 日期工具类，提供与日期操作相关的静态方法
 */
public class DateUtils {

    /**
     * 根据给定的时间差，格式化为天数或分钟数的字符串表示
     * 该方法主要用于展示两个时间点之间的差距，以用户友好的方式
     *
     * @param time        代表时间差的毫秒数
     * @param currentDate 当前时间点
     * @return 返回格式化后的时间差字符串
     */
    public static String formatTimeDifference(long time) {
        try {
            // 根据给定的时间差，从当前时间偏移得到新的时间点
            Date newDate = DateUtil.offsetMillisecond(new Date(), (int) time);

            // 格式化新时间点与当前时间之间的差值，精确到分钟
            return DateUtil.formatBetween(newDate, new Date(), BetweenFormatter.Level.MINUTE);
        } catch (Exception e) {
            // 记录异常日志
            e.printStackTrace();
            // 返回默认值或错误信息
            return "错误：无法格式化时差";
        }
    }


    /**
     * 格式化登录时间差
     * 该方法用于计算从当前时间起，到指定登录ID的禁用时间还有多久
     * 它会将禁用时间转换为相对于当前时间的未来时间点，并格式化显示这段时间差
     *
     * @param loginId 登录ID，可以是任何类型的对象，用于StpUtil获取禁用时间
     * @return 返回一个表示时间差的字符串，格式为"X天X小时X分钟"
     */
    public static String formatDays(Object loginId) {
        if (loginId == null) {
            throw new IllegalArgumentException("登录ID不能为空");
        }

        try {
            // 获取指定登录ID的禁用时间（单位：秒）
            long time = StpUtil.getDisableTime(loginId);

            // 检查返回的时间是否合理
            if (time < 0) {
                throw new IllegalArgumentException("无效禁用时间: " + time);
            }

            // 当前时间
            Date currentDate = new Date();

            // 根据给定的时间差，从当前时间偏移得到新的时间点
            Date newDate = DateUtil.offsetSecond(currentDate, (int) time);

            // 格式化新时间点与当前时间之间的差值，精确到分钟
            return DateUtil.formatBetween(newDate, currentDate, BetweenFormatter.Level.MINUTE);
        } catch (Exception e) {
            // 记录异常日志
            System.err.println("格式化错误天数: " + e.getMessage());

            // 返回默认值或空字符串
            return "";
        }
    }

}