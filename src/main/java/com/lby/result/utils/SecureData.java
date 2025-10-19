package com.lby.result.utils;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.ToString;

/**
 * SecureData 类用于存储加密后的密码数据及其对应的加密密钥。
 * 该类包含两个重要信息：加密后的密码和用于加密的密钥。
 * 使用 Lombok 注解自动生成 getter 方法和 toString 方法，简化代码并提高可维护性。
 *
 * @param encryptedPassword 加密后的密码，以字符串形式存储。
 * @param encryptionKey    加密密钥，用于加密密码，同样以字符串形式存储。
 */
@Getter
@ToString
@AllArgsConstructor
public class SecureData {
    // 加密后的密码
    private String encryptedPassword;
    // 加密密钥
    private String encryptionKey;
}
