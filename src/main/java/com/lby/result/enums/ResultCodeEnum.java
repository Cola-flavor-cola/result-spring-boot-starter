package com.lby.result.enums;

import lombok.Getter;

@Getter
public enum ResultCodeEnum {
    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    BIZ_ERROR(501, "通用业务异常"),
    FILE_OUT_MAX(502, "文件超出最大限制"),
    FILE_FORMAT_ERROR(503, "文件格式不正确"),
    PARAM_ERROR(504, "参数错误"),
    JSON_FORMAT_ERROR(505, "Json解析异常"),
    SQL_ERROR(506, "Sql解析异常"),
    NETWORK_TIMEOUT(507, "网络超时"),
    UNKNOWN_INTERFACE(508, "未知的接口"),
    REQ_MODE_NOT_SUPPORTED(509, "请求方式不支持"),
    SYS_ERROR(510, "系统异常");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态信息
     */
    private final String msg;

    public int getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }

    ResultCodeEnum(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}
