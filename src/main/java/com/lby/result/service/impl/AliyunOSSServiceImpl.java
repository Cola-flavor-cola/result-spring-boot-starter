package com.lby.result.service.impl;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.OSSException;
import com.aliyun.oss.common.auth.CredentialsProviderFactory;
import com.aliyun.oss.model.ObjectMetadata;
import com.lby.result.enums.FileUploadType;
import com.lby.result.service.AliyunOSSService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.Base64Utils;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AliyunOSSServiceImpl implements AliyunOSSService {

    private final String bucketName; // 存储空间名称，用于标识OSS中的具体存储位置
    private final OSSClient client; // OSS客户端，用于与OSS服务进行通信
    private final String folderName; // 文件夹名称，用于组织和管理OSS中的对象
    private final String ossAccessDomainUrl; // OSS访问域名，用于构造对象的访问URL
    private final FileUploadType fileUploadType; // 文件上传类型，定义了文件上传的策略或方式

    /**
     * 构造函数用于初始化AliyunOSSServiceImpl类的实例
     * 该构造函数接收多个参数，这些参数是从配置文件中读取的，用于配置和初始化OSS客户端
     *
     * @param bucketName         存储桶名称，用于存储文件
     * @param endpointUrl        OSS服务的接入地址
     * @param accessKeyId        阿里云访问密钥ID，用于身份验证
     * @param accessKeySecret    阿里云访问密钥密钥，用于身份验证
     * @param ossAccessDomainUrl OSS访问域名，用于构造文件访问URL
     * @param folderName         文件夹名称，用于在存储桶中组织文件
     * @param fileUploadType     文件上传类型，决定文件上传的策略
     */
    public AliyunOSSServiceImpl(
            @Value("${oss.alioss.bucketName}") String bucketName,
            @Value("${oss.alioss.endpointUrl}") String endpointUrl,
            @Value("${oss.alioss.accessKeyId}") String accessKeyId,
            @Value("${oss.alioss.accessKeySecret}") String accessKeySecret,
            @Value("${oss.alioss.domain}") String ossAccessDomainUrl,
            @Value("${oss.alioss.folderName}") String folderName,
            @Value("${oss.alioss.fileUploadType}") FileUploadType fileUploadType) {
        // 初始化存储桶名称
        this.bucketName = bucketName;
        // 初始化OSS访问域名
        this.ossAccessDomainUrl = ossAccessDomainUrl;
        // 初始化OSS客户端
        this.client = new OSSClient(endpointUrl, CredentialsProviderFactory.newDefaultCredentialProvider(accessKeyId, accessKeySecret), new ClientConfiguration());
        // 初始化文件夹名称
        this.folderName = folderName;
        // 初始化文件上传类型
        this.fileUploadType = fileUploadType;
    }


    /**
     * 根据保存文件夹和文件名获取真实的文件路径
     *
     * @param saveFolder 保存文件夹的路径，表示文件应保存的目录如果为空，表示直接使用文件名
     * @param fileName   文件名，是文件最终保存时的名称
     * @return 返回真实的文件路径，如果saveFolder为空，则直接返回fileName
     */
    private String getRealFileName(String saveFolder, String fileName) {
        // 如果saveFolder不为空，则将saveFolder和fileName拼接，形成完整的文件路径；否则，直接返回fileName
        return StringUtils.isNotEmpty(saveFolder) ? saveFolder + "/" + fileName : fileName;
    }

    /**
     * 上传文件到云存储服务
     * <p>
     * 此方法负责将给定的文件内容上传到指定的云存储桶中，并返回文件的访问URL
     * 它首先校验输入参数的有效性，然后设置文件的元数据，最后执行上传操作
     *
     * @param saveFolder    文件保存的文件夹路径
     * @param contentType   文件的内容类型
     * @param fileName      文件名
     * @param contentLength 文件内容的长度
     * @param input         文件内容的输入流
     * @return 如果上传成功，返回文件的访问URL；否则返回null
     */
    @Override
    public String upload(String saveFolder, String contentType, String fileName, long contentLength, InputStream input) {
        // 校验基础参数有效性（防止空指针）
        if (input == null || contentLength <= 0) {
            return null;
        }
        // 校验文件类型参数
        if (fileUploadType != null && !fileUploadType.isAllowedExtension(fileName)) {
            //当上传的文件与fileUploadType不匹配时抛出异常
            throw new IllegalArgumentException("文件类型不匹配");
        }

        // 校验文件名和内容类型
        if (StringUtils.isEmpty(fileName) || StringUtils.isEmpty(contentType)) {
            return null;
        }

        // 校验最大文件大小（示例：100MB）
        final long MAX_FILE_SIZE = 100 * 1024 * 1024; // 100MB
        if (contentLength > MAX_FILE_SIZE) {
            log.warn("文件大小超过限制: {}", fileName);
            return null;
        }

        // 设置文件元数据
        ObjectMetadata objectMeta = new ObjectMetadata();
        objectMeta.setContentLength(contentLength);
        objectMeta.setContentType(contentType);
        // 生成实际存储的文件路径
        String filePath = getRealFileName(saveFolder, fileName);
        try (InputStream is = input) {
            // 执行文件上传操作
            client.putObject(bucketName, filePath, input, objectMeta);
            // 返回文件的访问URL
            return ossAccessDomainUrl + filePath;
        } catch (IOException | OSSException e) {
            log.error("上传文件失败: {}", fileName, e);
            throw new RuntimeException("上传文件失败: " + fileName, e);
        }
    }

    /**
     * 上传文件方法
     *
     * @param saveFolder    存储文件的文件夹路径
     * @param fileMainName  文件主名称，用于生成最终的文件名
     * @param multipartFile 要上传的文件，通常来自HTTP请求
     * @return 返回上传后的文件路径或者null如果上传失败
     */
    @Override
    public String upload(String fileMainName, MultipartFile multipartFile) {
        // 检查文件是否已选择且不为空
        if (null != multipartFile && !multipartFile.isEmpty()) {
            try {
                // 获取文件原始名称
                String filename = multipartFile.getOriginalFilename();
                String extFileName = filename.substring(filename.lastIndexOf("."));
                // 调用upload方法上传文件，使用文件主名称加上扩展名作为文件名
                return upload(folderName, multipartFile.getContentType(), fileMainName + extFileName, multipartFile.getSize(), multipartFile.getInputStream());
            } catch (IOException e) {
                // 打印异常信息
                e.printStackTrace();
            }
        }
        // 如果文件为空或者上传过程中发生异常，返回null
        return null;
    }

    /**
     * 用于处理特殊命名的文件上传
     * 此方法专注于处理具有特殊名称的文件上传，与普通的文件上传方法相比，它更强调文件名的特殊性
     *
     * @param saveFolder    文件保存的文件夹路径
     * @param fileName      文件名，对于具有特殊命名规则的文件，此参数可能包含特定的格式或前缀
     * @param multipartFile 包含文件内容的multipart文件对象
     * @return 返回上传后的文件路径或者null，如果上传失败
     */
    @Override
    public String upload4SpecialName(FileUploadType fileUploadType, String saveFolder, String fileName, MultipartFile multipartFile) {
        // 检查multipartFile对象是否非空且不为空文件
        if (null != multipartFile && !multipartFile.isEmpty()) {
            try {
                // 调用upload方法执行文件上传，参数包括保存文件夹、文件类型、文件名、文件大小和文件输入流
                return upload(saveFolder, multipartFile.getContentType(), fileName, multipartFile.getSize(), multipartFile.getInputStream());
            } catch (IOException e) {
                // 捕获IOException并打印堆栈跟踪，表明在文件上传过程中遇到I/O错误
                e.printStackTrace();
            }
        }
        // 如果multipartFile为空或者上传过程中出现异常，返回null
        return null;
    }

    /**
     * 使用Base64编码的字符串上传图片
     * 此方法接收保存文件的文件夹路径、文件名和Base64编码的图片内容，然后调用上传方法进行图片上传
     * 如果文件名或Base64编码的图片内容为空，则返回null
     *
     * @param saveFolder       保存图片的文件夹路径
     * @param fileName         图片文件名
     * @param base64ImgContent Base64编码的图片内容
     * @return 上传成功返回上传后的图片路径，上传失败返回null
     */
    @Override
    public String uploadImgByBase64(String saveFolder, String fileName, String base64ImgContent) {
        // 检查文件名和Base64编码的图片内容是否为空
        if (StringUtils.isEmpty(fileName) || StringUtils.isEmpty(base64ImgContent)) {
            return null;
        }
        try {
            // 将Base64编码的字符串解码为字节数组
            byte[] bytes = Base64Utils.decodeFromString(base64ImgContent);
            // 调用上传方法，传入文件夹路径、文件名、字节数组长度和字节数组输入流
            return upload(saveFolder, "image/jpg", fileName, bytes.length, new ByteArrayInputStream(bytes));
        } catch (Exception e) {
            // 异常处理：打印异常信息并返回null
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 重写delete方法，用于删除指定的文件
     *
     * @param fileUrl 文件的URL地址，用于定位要删除的文件
     */
    @Override
    public void delete(String fileUrl) {
        // 检查文件URL是否为空，如果为空则直接返回，不执行删除操作
        if (StringUtils.isEmpty(fileUrl)) {
            return;
        }
        try {
            // 移除文件URL中的访问域名部分，获取纯路径
            fileUrl = fileUrl.replaceFirst(ossAccessDomainUrl, "");
            // 调用客户端API，根据桶名称和文件路径删除指定文件
            client.deleteObject(bucketName, fileUrl);
        } catch (OSSException | ClientException e) {
            // 捕获可能的异常并打印堆栈跟踪，OSSException和ClientException分别表示OSS操作异常和客户端异常
            e.printStackTrace();
        }
    }

    /**
     * 批量删除文件
     *
     * @param fileUrls 文件URL列表，用于指定待删除的文件
     */
    @Override
    public void batchDelete(List<String> fileUrls) {
        // 如果文件URL列表为空或为null，则直接返回，不执行删除操作
        if (fileUrls == null || fileUrls.isEmpty()) {
            return;
        }

        // 使用CompletableFuture异步执行文件删除任务
        List<CompletableFuture<Void>> futures = fileUrls.stream()
                // 过滤掉空或空白的URL
                .filter(url -> StringUtils.isNotBlank(url))
                // 将每个文件URL映射为一个CompletableFuture任务
                .map(url -> CompletableFuture.runAsync(() -> {
                    try {
                        // 从URL中提取出文件在OSS中的键值
                        String key = url.replaceFirst(ossAccessDomainUrl, "");
                        // 调用OSS客户端的删除方法删除文件
                        client.deleteObject(bucketName, key);
                        // 打印删除成功的消息
                        System.out.println("删除文件: " + url);
                    } catch (OSSException | ClientException e) {
                        // 如果删除失败，打印错误消息
                        System.err.println("删除文件失败: " + url + ", 错误: " + e.getMessage());
                    }
                }))
                // 将流转换为列表，以便后续处理
                .collect(Collectors.toList());

        // 使用CompletableFuture.allOf等待所有删除任务完成
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                // 如果所有任务都完成且没有异常，则join方法会返回null
                .exceptionally(ex -> {
                    // 如果有任务抛出异常，则打印错误消息
                    System.err.println("批量删除遇到错误: " + ex.getMessage());
                    return null;
                })
                // 等待所有任务完成
                .join();
    }
}