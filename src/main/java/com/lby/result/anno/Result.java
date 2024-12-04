package com.lby.result.anno;

import org.springframework.core.annotation.AliasFor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ResponseBody;
import java.lang.annotation.*;


/**
 * 定义了一个名为Result的自定义注解。
 * 该注解结合了@Controller和@ResponseBody的功能。
 *
 * @Retention(RetentionPolicy.RUNTIME) 表示该注解在运行时仍然有效。
 * @Target({ElementType.TYPE, ElementType.METHOD}) 表示该注解可以用于类或方法上。
 * @Documented 表示该注解将被集成到JavaDoc中。
 * @Controller 标识一个类既是控制器又是业务逻辑处理类。
 * @ResponseBody 表明该类或方法将直接返回数据，而不是视图页面。
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Controller
@ResponseBody
public @interface Result {
    /**
     * 获取注解的默认值。
     *
     * @return 默认值为空字符串
     */
    @AliasFor(annotation = Controller.class)
    String value() default "";
}
