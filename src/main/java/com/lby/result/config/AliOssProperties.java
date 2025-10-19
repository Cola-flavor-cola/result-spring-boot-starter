package com.lby.result.config;

import com.lby.result.enums.FileUploadType;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * 用于封装阿里云OSS对象存储服务的配置属性，通过@ConfigurationProperties自动绑定配置文件中以"oss.alioss"为前缀的属性。
 * 该类被Spring容器管理，可直接通过依赖注入获取配置值。
 *
 * 字段说明：
 * - endpoint: OSS服务的访问端点地址（如oss-cn-beijing.aliyuncs.com）
 * - accessKeyId: 阿里云账号的访问密钥ID，用于身份认证
 * - accessKeySecret: 阿里云账号的访问密钥私钥，用于签名和鉴权
 * - bucketName: 需要操作的OSS存储空间名称
 */
@Data
@Component
@ConfigurationProperties(prefix = "oss.alioss")
public class AliOssProperties {

    /**
     * OSS服务终端节点，用于指定OSS服务的接入地址
     */
    private String endpointUrl;

    /**
     * 阿里云访问密钥ID，用于身份验证
     */
    private String accessKeyId;

    /**
     * 阿里云访问密钥，与accessKeyId一起用于身份验证
     */
    private String accessKeySecret;

    /**
     * OSS服务中的存储空间名称
     */
    private String bucketName;

    /**
     * OSS服务中的存储空间名称
     */
    private String domain;

    /**
     * 文件上传的目录名称
     */
    private String folderName;

    /**
     * 文件上传类型
     */
    private FileUploadType fileUploadType;

}