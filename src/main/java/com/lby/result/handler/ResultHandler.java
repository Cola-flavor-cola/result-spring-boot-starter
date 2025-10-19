package com.lby.result.handler;

import cn.dev33.satoken.secure.SaSecureUtil;
import cn.dev33.satoken.stp.StpUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.lby.result.anno.Result;
import com.lby.result.config.ResultProperties;
import com.lby.result.utils.R;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
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
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class ResultHandler implements ResponseBodyAdvice<Object> {

    private final ResultProperties properties;
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String SUCCESS_MESSAGE = "成功";
    private static final String ERROR_MESSAGE = "处理响应体时发生异常";
    private static final String HEADER_ENCRYPT_OPTION = "X-Content-Encrypted";
    private static final Logger logger = LoggerFactory.getLogger(ResultHandler.class);

    static {
        OBJECT_MAPPER.registerModule(new JavaTimeModule());
    }

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
     * @param body                  原始返回值对象
     * @param returnType            方法参数类型
     * @param selectedContentType   选定的内容类型
     * @param selectedConverterType 选定的消息转换器类型
     * @param request               当前请求对象
     * @param response              当前响应对象
     * @return 处理后的返回值对象
     */
    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        try {
            if (body instanceof R) {
                return body;
            }
            return R.success(SUCCESS_MESSAGE, body);
        } catch (Exception e) {
            logger.error(ERROR_MESSAGE, e); // 记录完整异常堆栈
            return R.error(ERROR_MESSAGE);
        }
    }

}