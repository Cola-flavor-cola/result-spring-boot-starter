package com.lby.result.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Collections;
import java.util.List;

@Configuration
public class WebMvcConfiguration implements WebMvcConfigurer {

    /**
     * 交换 MappingJackson2HttpMessageConverter 与第一位元素
     * 让返回值类型为String的接口能正常返回包装结果
     *
     * @param converters 初始时为一个空的转换器列表
     */
    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        if (converters == null || converters.isEmpty()) {
            return;
        }

        int targetIndex = -1;
        for (int i = 0; i < converters.size(); i++) {
            HttpMessageConverter<?> converter = converters.get(i);
            if (converter.getClass().equals(MappingJackson2HttpMessageConverter.class)) {
                targetIndex = i;
                break;
            }
        }

        if (targetIndex > 0) {
            Collections.swap(converters, 0, targetIndex);
        }
    }


}
