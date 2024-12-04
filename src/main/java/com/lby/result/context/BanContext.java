package com.lby.result.context;

import cn.dev33.satoken.stp.StpUtil;
import com.lby.result.dto.BannedUserInfo;
import com.lby.result.strategy.BanStrategy;
import lombok.AllArgsConstructor;
import lombok.Setter;

@Setter
@AllArgsConstructor
public class BanContext {

    private BanStrategy strategy;

    /**
     * 根据提供的用户信息禁用用户
     * 该方法首先计算用户的禁用时间，然后禁用用户，并最后执行登出操作
     *
     * @param dto 包含用户信息的BannedDto对象，用于禁用用户
     */
    public void disableUser(BannedUserInfo dto) {
        // 计算用户的禁用时间
        long banTime = strategy.calculateBanTime(dto);

        // 禁用用户，禁用时长为banTime
        StpUtil.disable(dto.getId(), banTime);

        // 执行用户登出操作
        StpUtil.logout(dto.getId());
    }
}