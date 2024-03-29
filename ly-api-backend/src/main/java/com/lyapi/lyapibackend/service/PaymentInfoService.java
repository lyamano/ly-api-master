package com.lyapi.lyapibackend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.lyapi.lyapibackend.model.vo.PaymentInfoVo;
import com.lyapi.lyapibackend.model.entity.PaymentInfo;

/**
* @Author: Liu
* @Date: 2023/08/23 08:16:11
* @Version: 1.0
* @Description: 支付信息服务
*/
public interface PaymentInfoService extends IService<PaymentInfo> {
   /**
    * 创建付款信息
    *
    * @param paymentInfoVo 付款信息vo
    * @return boolean
    */
   boolean createPaymentInfo(PaymentInfoVo paymentInfoVo);
}
