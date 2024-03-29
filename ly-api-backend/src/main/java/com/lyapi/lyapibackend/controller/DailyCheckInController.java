package com.lyapi.lyapibackend.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lyapi.lyapibackend.common.BaseResponse;
import com.lyapi.lyapibackend.common.ErrorCode;
import com.lyapi.lyapibackend.common.ResultUtils;
import com.lyapi.lyapibackend.exception.BusinessException;
import com.lyapi.lyapibackend.model.entity.DailyCheckIn;
import com.lyapi.lyapibackend.model.vo.UserVO;
import com.lyapi.lyapibackend.service.DailyCheckInService;
import com.lyapi.lyapibackend.service.UserService;
import com.lyapi.lyapibackend.utils.RedissonLockUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.ObjectUtils;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.TimeUnit;

/**
 * @Author: Liu
 * @Date: 2023/08/31 11:51:14
 * @Version: 1.0
 * @Description: 签到接口
 */
@RestController
@RequestMapping("/dailyCheckIn")
@Slf4j
public class DailyCheckInController {

    @Resource
    private DailyCheckInService dailyCheckInService;

    @Resource
    private UserService userService;
    @Resource
    private RedissonClient redissonClient;

    @Resource
    private RedissonLockUtil redissonLockUtil;

    // region 增删改查

    /**
     * 签到
     *
     * @param request 请求
     * @return {@link BaseResponse}<{@link Boolean}>
     */
    // @PostMapping("/doCheckIn")
    // @Transactional(rollbackFor = Exception.class)
    // public BaseResponse<Boolean> doDailyCheckIn(HttpServletRequest request) {
    //     UserVO loginUser = userService.getLoginUser(request);
    //     String redissonLock = ("doDailyCheckIn_" + loginUser.getUserAccount()).intern();
    //     return redissonLockUtil.redissonDistributedLocks(redissonLock, () -> {
    //         LambdaQueryWrapper<DailyCheckIn> dailyCheckInLambdaQueryWrapper = new LambdaQueryWrapper<>();
    //         dailyCheckInLambdaQueryWrapper.eq(DailyCheckIn::getUserId, loginUser.getId());
    //         DailyCheckIn dailyCheckIn = dailyCheckInService.getOne(dailyCheckInLambdaQueryWrapper);
    //         if (ObjectUtils.isNotEmpty(dailyCheckIn)) {
    //             throw new BusinessException(ErrorCode.OPERATION_ERROR, "签到失败,今日已签到");
    //         }
    //         dailyCheckIn = new DailyCheckIn();
    //         dailyCheckIn.setUserId(loginUser.getId());
    //         dailyCheckIn.setAddPoints(10);
    //         boolean dailyCheckInResult = dailyCheckInService.save(dailyCheckIn);
    //         boolean addWalletBalance = userService.addWalletBalance(loginUser.getId(), dailyCheckIn.getAddPoints());
    //         boolean result = dailyCheckInResult & addWalletBalance;
    //         if (!result) {
    //             throw new BusinessException(ErrorCode.OPERATION_ERROR);
    //         }
    //         return ResultUtils.success(true);
    //     }, "签到失败");
    // }
    @PostMapping("/doCheckIn")
    @Transactional(rollbackFor = Exception.class)
    public BaseResponse<Boolean> doDailyCheckIn(HttpServletRequest request) {
        UserVO loginUser = userService.getLoginUser(request);

        // 使用分布式锁控制并发访问
        String lockKey = "checkin_" + loginUser.getId();
        RLock redissonLock = redissonClient.getLock(lockKey);
        boolean success = false;

        try {
            boolean isLocked;
            try {
                isLocked = redissonLock.tryLock(3, 30, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // 恢复中断状态
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "签到失败，请稍后重试");
            }

            if (!isLocked) {
                return BaseResponse.fail("签到失败，请稍后重试");
            }

            // 检查用户是否已经签到过
            LambdaQueryWrapper<DailyCheckIn> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DailyCheckIn::getUserId, loginUser.getId());
            queryWrapper.apply("DATE_FORMAT(updateTime, '%Y-%m-%d') = CURDATE()");

            DailyCheckIn dailyCheckIn = dailyCheckInService.getOne(queryWrapper);
            if (ObjectUtils.isNotEmpty(dailyCheckIn)) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR, "签到失败,今日已签到");
            }

            // 如果用户今天还未签到，则进行签到操作
            dailyCheckIn = new DailyCheckIn();
            dailyCheckIn.setUserId(loginUser.getId());
            dailyCheckIn.setAddPoints(10); // 签到增加的积分数

            boolean dailyCheckInResult = dailyCheckInService.save(dailyCheckIn);
            boolean addWalletBalance = userService.addWalletBalance(loginUser.getId(), dailyCheckIn.getAddPoints());
            boolean result = dailyCheckInResult & addWalletBalance;
            if (!result) {
                throw new BusinessException(ErrorCode.OPERATION_ERROR);
            }
            success = true;
        } finally {
            redissonLock.unlock();
        }
        return success ? ResultUtils.success(true) : ResultUtils.error(ErrorCode.OPERATION_ERROR, "签到失败");
    }

    // endregion
}
