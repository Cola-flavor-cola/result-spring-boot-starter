package com.lby.result.dto;


import com.lby.result.enums.BanTime;
import lombok.Data;

/**
 * 封禁信息DTO（Data Transfer Object）
 * 用于在不同层之间传输被封禁用户的信息
 */
@Data
public class BannedUserInfo {
    private Integer id; // 用户ID，用于唯一标识被封禁的用户
    private Integer banTime; // 封禁时长，单位为秒，表示用户被封禁的时间长度
    private BanTime type; // 封禁类型，使用枚举类型BanTime来表示封禁的不同类型或原因
    private String reason; // 封禁原因，用于记录用户被封禁的具体原因，方便审核或申诉
}
