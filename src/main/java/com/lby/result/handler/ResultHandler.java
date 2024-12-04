package com.lby.result.handler;

import com.lby.result.anno.Result;
import com.lby.result.utils.R;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

/**
 * 自定义的 ResponseBodyAdvice 实现类，用于在返回响应体之前对返回值进行处理。
 * 主要功能包括：
 * 1. 检查方法或类是否注解了 @Result 注解。
 * 2. 如果返回值不是 R 类型，则将其封装成 R 类型的响应对象。
 */
@ControllerAdvice
public class ResultHandler implements ResponseBodyAdvice<Object> {

    /**
     * 判断是否支持给定的返回类型和转换器类型。
     * <p>
     * 此方法用于确定是否应该使用特定的 HttpMessageConverter 来处理方法返回值。
     * 它主要检查返回类型上是否直接或间接注解了 @Result 注解，以决定是否支持该转换器类型。
     *
     * @param returnType    方法返回类型，用于检查是否注解了 @Result 注解。
     * @param converterType HttpMessageConverter 类型，此方法不直接使用此参数，但子类可能需要。
     * @return 如果返回类型或其所在方法或类上注解了 @Result 注解，则返回 true；否则返回 false。
     */
    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        // 检查返回类型或其所在方法或类上是否注解了 @Result 注解
        return returnType.hasMethodAnnotation(Result.class) ||
                returnType.getContainingClass().isAnnotationPresent(Result.class);
    }

    /**
     * 在写入响应体之前，对返回值进行处理。
     *
     * @param body 原始返回值对象
     * @param returnType 方法参数类型
     * @param selectedContentType 选定的内容类型
     * @param selectedConverterType 选定的消息转换器类型
     * @param request 当前请求对象
     * @param response 当前响应对象
     * @return 处理后的返回值对象
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            // 如果返回值已经是封装好的 R 类型，则直接返回，不做处理
            if (body instanceof R) {
                return body;
            }
            // 如果响应体通过所有检查，则使用统一的响应对象进行包装，并返回
            return R.success("成功", body);
        } catch (Exception e) {
            // 记录异常日志
            System.err.println("处理响应体时发生异常: " + e.getMessage());
            // 返回默认的错误响应
            return R.error("处理响应体时发生异常");
        }
    }
}