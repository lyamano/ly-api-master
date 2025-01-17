package com.lyapi.lyapibackend.service.inner.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyapi.lyapibackend.common.ErrorCode;
import com.lyapi.lyapibackend.exception.BusinessException;
import com.lyapi.lyapibackend.mapper.UserInterfaceInvokeMapper;
import com.lyapi.lyapibackend.service.InterfaceInfoService;
import com.lyapi.lyapibackend.service.UserService;
import com.lyapi.lyapicommon.model.entity.UserInterfaceInvoke;
import com.lyapi.lyapicommon.service.inner.InnerUserInterfaceInvokeService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @Author: Liu
 * @Date: 2023/09/04 11:30:02
 * @Version: 1.0
 * @Description: 用户界面调用服务impl
 */
@DubboService
public class UserInterfaceInvokeServiceImpl extends ServiceImpl<UserInterfaceInvokeMapper, UserInterfaceInvoke>
        implements InnerUserInterfaceInvokeService {
    @Resource
    private InterfaceInfoService interfaceInfoService;
    @Resource
    private UserService userService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public boolean invoke(Long interfaceInfoId, Long userId, Integer reduceScore) {
        LambdaQueryWrapper<UserInterfaceInvoke> invokeLambdaQueryWrapper = new LambdaQueryWrapper<>();
        invokeLambdaQueryWrapper.eq(UserInterfaceInvoke::getInterfaceId, interfaceInfoId);
        invokeLambdaQueryWrapper.eq(UserInterfaceInvoke::getUserId, userId);
        UserInterfaceInvoke userInterfaceInvoke = this.getOne(invokeLambdaQueryWrapper);
        // 不存在就创建一条记录
        boolean invokeResult;
        if (userInterfaceInvoke == null) {
            userInterfaceInvoke = new UserInterfaceInvoke();
            userInterfaceInvoke.setInterfaceId(interfaceInfoId);
            userInterfaceInvoke.setUserId(userId);
            userInterfaceInvoke.setTotalInvokes(1L);
            invokeResult = this.save(userInterfaceInvoke);
        } else {
            LambdaUpdateWrapper<UserInterfaceInvoke> invokeLambdaUpdateWrapper = new LambdaUpdateWrapper<>();
            invokeLambdaUpdateWrapper.eq(UserInterfaceInvoke::getInterfaceId, interfaceInfoId);
            invokeLambdaUpdateWrapper.eq(UserInterfaceInvoke::getUserId, userId);
            invokeLambdaUpdateWrapper.setSql("totalInvokes = totalInvokes + 1");
            invokeResult = this.update(invokeLambdaUpdateWrapper);
        }
        // 更新接口总调用次数
        boolean interfaceUpdateInvokeSave = interfaceInfoService.updateTotalInvokes(interfaceInfoId);
        // 更新用户钱包积分
        boolean reduceWalletBalanceResult = userService.reduceWalletBalance(userId, reduceScore);
        boolean updateResult = invokeResult && interfaceUpdateInvokeSave && reduceWalletBalanceResult;
        if (!updateResult) {
            throw new BusinessException(ErrorCode.OPERATION_ERROR, "调用失败");
        }
        return true;
    }
}




