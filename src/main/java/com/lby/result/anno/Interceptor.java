package com.lby.result.anno;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 定义一个拦截器注解，用于标注拦截器类。
 * 拦截器是在应用程序运行时，对特定方法的调用进行拦截的组件，可以用于实现如权限验证、日志记录等功能。
 * 该注解继承自@Component，因此也具有组件的特性，可以被Spring容器管理。
 *
 * @Target(ElementType.TYPE) 指定该注解可以用于类型（类）上。
 * @Retention(RetentionPolicy.RUNTIME) 指定该注解在运行时可见，可以被反射机制读取。
 * @Documented 将该注解包含在Javadoc中。
 * @Component 表明该注解是一个组件注解，可以被Spring容器识别和管理。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Interceptor {
    /**
     * 该属性与@Component注解的value属性相同，用于指定组件的名称。
     * 默认为空字符串，表示使用类名作为组件的名称。
     *
     * @return 组件的名称。
     */
    @AliasFor(annotation = Component.class)
    String value() default "";
}

