package com.lby.result.enums;
import java.util.Arrays;
import java.util.List;

/**
 * 文件上传类型枚举
 * 定义不同类型的文件存储路径及允许的扩展名
 */
public enum FileUploadType {

    /**
     * 图片文件类型：支持 jpg/jpeg/png/gif 格式，存储路径 images/
     */
    IMAGE("images", Arrays.asList(".jpg", ".jpeg", ".png", ".gif",  ".bmp", ".tif", ".tiff")),
    
    /**
     * 文档文件类型：支持 pdf/doc/docx/xls/xlsx/ppt/pptx 格式，存储路径 documents/
     */
    DOCUMENT("documents", Arrays.asList(".pdf", ".doc", ".docx", ".xls", ".xlsx", ".ppt", ".pptx")),
    
    /**
     * 视频文件类型：支持 mp4/avi/mov/mkv 格式，存储路径 videos/
     */
    VIDEO("videos", Arrays.asList(".mp4", ".avi", ".mov", ".mkv")),
    
    /**
     * 音频文件类型：支持 mp3/wav/ogg 格式，存储路径 audios/
     */
    AUDIO("audios", Arrays.asList(".mp3", ".wav", ".ogg"));

    private final String fileFolder;
    private final List<String> allowedExtensions;

    FileUploadType(String fileFolder, List<String> allowedExtensions) {
        this.fileFolder = fileFolder;
        this.allowedExtensions = allowedExtensions;
    }

    /**
     * 获取文件存储路径
     * @return 文件夹路径
     */
    public String getFileFolder() {
        return fileFolder;
    }

    /**
     * 检查文件扩展名是否合法
     *
     * 该方法通过验证文件名是否包含有效扩展名，并判断其是否在允许的扩展名列表中，
     * 主要用于文件上传时的安全校验场景。
     *
     * @param fileName 需要校验的文件名（包含扩展名）
     * @return boolean 返回校验结果：true表示允许上传，false表示禁止上传
     *
     * 重要逻辑说明：
     * 1. 空文件名或不含扩展名的文件名直接拒绝
     * 2. 扩展名比较时会转换为小写格式进行匹配（确保大小写不敏感）
     * 3. 最终校验结果依赖allowedExtensions集合的配置内容
     */
    public boolean isAllowedExtension(String fileName) {
        /**
         * 初始校验逻辑：
         * - 检查文件名是否为空
         * - 验证是否包含扩展名标识符（点号）
         * 不符合任一条件则直接返回false
         */
        if (fileName == null || !fileName.contains(".")) {
            return false;
        }

        /**
         * 扩展名提取与格式标准化：
         * 1. 从最后一个点号位置截取扩展名部分
         * 2. 统一转换为小写格式以消除大小写差异
         * 示例：".TXT" -> ".txt"，"README.md" -> ".md"
         */
        String extension = fileName.substring(fileName.lastIndexOf(".")).toLowerCase();

        /**
         * 最终扩展名校验：
         * 检查标准化后的扩展名是否包含在预定义的allowedExtensions集合中
         * allowedExtensions应为类成员变量，包含允许上传的扩展名列表
         */
        return allowedExtensions.contains(extension);
    }

}