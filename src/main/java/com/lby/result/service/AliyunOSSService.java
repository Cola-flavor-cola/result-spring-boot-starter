package com.lby.result.service;


import com.lby.result.enums.FileUploadType;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;

/**
 * 阿里云的开放式存储服务API接口层
 */
public interface AliyunOSSService {

    /**
     * 上传文件到云存储服务
     * <p>
     * 此方法负责将给定的文件内容上传到指定的云存储桶中，并返回文件的访问URL
     * 它首先校验输入参数的有效性，然后设置文件的元数据，最后执行上传操作
     * 
     * @param fileUploadType 文件上传类型枚举
     * @param contentType   文件的内容类型
     * @param fileName      文件名
     * @param contentLength 文件内容的长度
     * @param input         文件内容的输入流
     * @return 如果上传成功，返回文件的访问URL；否则返回null
     */
    String upload(String saveFolder, String contentType, String fileName, long contentLength, InputStream input);

    /**
     * 上传文件方法
     * 
     * @param fileUploadType 文件上传类型枚举
     * @param fileMainName  文件主名称，用于生成最终的文件名
     * @param multipartFile 要上传的文件，通常来自HTTP请求
     * @return 返回上传后的文件路径或者null如果上传失败
     */
    String upload(String fileMainName, MultipartFile multipartFile);

    /**
     * 无结尾类型，直接上传
     * @author DuanLinpeng
     * @date 2021/01/16 21:31
     * @param fileUploadType 文件上传类型枚举
     * @param saveFolder 存储的地址路径
     * @param fileName  文件名称
     * @param multipartFile  文件流
     * @return java.lang.String
     */
    String upload4SpecialName(FileUploadType fileUploadType, String saveFolder, String fileName, MultipartFile multipartFile);

    /**
     *   把base64字符串转换为流存到oss中
     * @author DuanLinpeng
     * @date 2021/01/16 21:28
     * @param saveFolder  存储的地址路径
     * @param fileName  文件名称
     * @param base64ImgContent   图片转base64字符串
     * @return java.lang.String 返回图片地址
     */
    String uploadImgByBase64(String saveFolder, String fileName, String base64ImgContent);


    /**
     *  删除OSS中 fileUrl 的文件
     * @author DuanLinpeng
     * @date 2021/01/16 21:28
     * @param fileUrl
     * @return void
     */
    void delete(String fileUrl);

    /**
     * 批量删除文件
     *
     * @param fileUrls 文件URL列表，表示待删除的文件资源位置
     */
    void batchDelete(List<String> fileUrls);
}