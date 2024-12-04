package com.lby.result.config;

import com.lby.result.handler.ResultHandler;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.annotation.Import;

@AutoConfiguration
@Import({ResultHandler.class,ResultProperties.class, BeanConverterProperties.class})
public class ResultAutoConfig {



}