package com.lyapi.lyapibackend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lyapi.lyapibackend.mapper.DailyCheckInMapper;
import com.lyapi.lyapibackend.model.entity.DailyCheckIn;
import com.lyapi.lyapibackend.service.DailyCheckInService;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

/**
 * @Author: Liu
 * @Date: 2023/08/31 11:47:57
 * @Version: 1.0
 * @Description: 每日签到服务impl
 */
@Service
public class DailyCheckInServiceImpl extends ServiceImpl<DailyCheckInMapper, DailyCheckIn>
        implements DailyCheckInService {

}




