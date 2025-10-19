package com.lby.result.config;

import com.lby.result.service.AliyunOSSService;
import com.lby.result.service.impl.AliyunOSSServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class OssConfiguration {
    @Bean
    @ConditionalOnBean
    public AliyunOSSService aliyunOSSService(AliOssProperties aliOssProperties) {
        log.info("初始化阿里云OSS服务");
        return new AliyunOSSServiceImpl(
                aliOssProperties.getBucketName(),
                aliOssProperties.getEndpointUrl(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getDomain(),
                aliOssProperties.getFolderName(),
                aliOssProperties.getFileUploadType()
        );
    }
}