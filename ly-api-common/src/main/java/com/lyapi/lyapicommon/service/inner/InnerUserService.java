package com.lyapi.lyapicommon.service.inner;

import com.lyapi.lyapicommon.model.vo.UserVO;


/**
 * @Author: Liu
 * @Date: 2023/08/21 10:06:40
 * @Version: 1.0
 * @Description: 用户服务
 */
public interface InnerUserService {

    /**
     * 通过访问密钥获取invoke用户
     * 按凭证获取invoke用户
     *
     * @param accessKey 访问密钥
     * @return {@link UserVO}
     */
    UserVO getInvokeUserByAccessKey(String accessKey);
}
