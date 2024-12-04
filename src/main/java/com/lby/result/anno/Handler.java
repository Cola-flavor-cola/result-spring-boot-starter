package com.lby.result.anno;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;

/**
 * 定义了一个处理程序的注解，用于标记特定类型的组件。
 *
 * 该注解的主要目的是为了在运行时识别和管理那些实现了特定业务逻辑或处理功能的组件。
 * 通过使用这个注解，可以在程序中将这些组件注册到一个管理器中，以便于动态地发现和使用它们。
 *
 * @Retention 这个注解指示 Handler 注解在运行时是可见的，允许程序在运行时通过反射机制读取该注解的信息。
 * @Target 这个注解指示 Handler 注解应该被应用到类型（类、接口等）上。
 * @Component 这个注解表明 Handler 是一个 Spring 组件，可以被 Spring 的依赖注入机制管理。
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Component
public @interface Handler {
    /**
     * 该方法用于指定组件的名称，这是一个可选的配置项。
     * 默认情况下，如果该方法没有被赋值，组件的名称将由 Spring 自动生成。
     *
     * 使用该方法可以为组件指定一个更容易理解和识别的名称，特别是在组件需要被其他组件引用时，
     * 一个清晰的名称可以提高代码的可读性和可维护性。
     *
     * @return 组件的名称，返回一个字符串。默认情况下为空字符串，表示使用 Spring 自动生成的名称。
     */
    @AliasFor(annotation = Component.class)
    String value() default "";

}
