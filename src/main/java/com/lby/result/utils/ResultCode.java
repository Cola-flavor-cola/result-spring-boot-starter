package com.lby.result.utils;

import com.lby.result.enums.ResultCodeEnum;
import lombok.Builder;
import lombok.Data;

import java.io.Serializable;

@Data
@Builder
public class ResultCode implements Serializable {

    private static final long serialVersionUID = -6269841958947880397L;

    /**
     * 状态码
     */
    private int code;

    /**
     * 状态信息
     */
    private String msg;

    /**
     * 默认成功
     */
    public final static ResultCode SUCCESS = dispose(ResultCodeEnum.SUCCESS);
    /**
     * 默认失败
     */
    public final static ResultCode ERROR = dispose(ResultCodeEnum.ERROR);
    /**
     * 通用业务异常
     */
    public final static ResultCode BIZ_ERROR = dispose(ResultCodeEnum.BIZ_ERROR);
    /**
     * 文件超出最大限制
     */
    public final static ResultCode FILE_OUT_MAX = dispose(ResultCodeEnum.FILE_OUT_MAX);
    /**
     * 文件格式不正确
     */
    public final static ResultCode FILE_FORMAT_ERROR = dispose(ResultCodeEnum.FILE_FORMAT_ERROR);
    /**
     * 参数错误
     */
    public final static ResultCode PARAM_ERROR = dispose(ResultCodeEnum.PARAM_ERROR);
    /**
     * Json解析异常
     */
    public final static ResultCode JSON_FORMAT_ERROR = dispose(ResultCodeEnum.JSON_FORMAT_ERROR);
    /**
     * Sql解析异常
     */
    public final static ResultCode SQL_ERROR = dispose(ResultCodeEnum.SQL_ERROR);
    /**
     * 网络超时
     */
    public final static ResultCode NETWORK_TIMEOUT = dispose(ResultCodeEnum.NETWORK_TIMEOUT);
    /**
     * 未知的接口
     */
    public final static ResultCode UNKNOWN_INTERFACE = dispose(ResultCodeEnum.UNKNOWN_INTERFACE);
    /**
     * 请求方式不支持
     */
    public final static ResultCode REQ_MODE_NOT_SUPPORTED = dispose(ResultCodeEnum.REQ_MODE_NOT_SUPPORTED);
    /**
     * 系统异常
     */
    public final static ResultCode SYS_ERROR = dispose(ResultCodeEnum.SYS_ERROR);

    private static ResultCode dispose(ResultCodeEnum codeEnum) {
        return ResultCode.builder().code(codeEnum.getCode()).msg(codeEnum.getMsg()).build();
    }

    public ResultCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}