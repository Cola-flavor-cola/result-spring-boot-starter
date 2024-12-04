package com.lby.result.anno;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 定义一个Listener注解，用于标识监听器类
 * 该注解可以被Java反射机制使用，并在运行时被保留
 * 同时，它也将会被文档工具所记录
 *
 * @Target(ElementType.TYPE) 表示该注解可以应用于类或接口
 * @Retention(RetentionPolicy.RUNTIME) 表示该注解在运行时仍然有效
 * @Documented 表示该注解将被包含在API文档中
 * @Component 表示该注解是一个Spring组件，可以被Spring框架自动扫描和管理
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Listener {
    /**
     * 定义Listener注解的value属性，用于指定被注解类的别名
     * 该属性使用了AliasFor注解，表示它是一个别名，其实际含义和默认值由Component注解决定
     *
     * @return String 类的别名，默认为空字符串
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}
