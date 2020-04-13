package org.loyer.dingtalk.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiUserGetRequest;
import com.dingtalk.api.response.OapiUserGetResponse;
import com.taobao.api.ApiException;
import org.loyer.dingtalk.config.URLConstant;
import org.loyer.dingtalk.util.AccessTokenUtil;
import org.loyer.dingtalk.util.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
public class ManagerController {
    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    @RequestMapping(value = "/manage", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResult manage(@RequestParam(value = "userId") String userId) {

        logger.info("获取当前用户的信息");
        Map<String, Object> resultMap = new HashMap<>();

        try {
            //获取accessToken
            String accessToken = AccessTokenUtil.getToken();
            logger.info("accessToken = " + accessToken);

            //获取用户信息
            DingTalkClient clt = new DefaultDingTalkClient(URLConstant.URL_USER_GET);
            OapiUserGetRequest rqt = new OapiUserGetRequest();
            rqt.setUserid(userId);
            rqt.setHttpMethod("GET");
            OapiUserGetResponse rsp = clt.execute(rqt, accessToken);
            //将结果转换成JSON对象
            JSONObject userInfo = JSON.parseObject(rsp.getBody());
            //添加结果集到返回数据中
            resultMap.put("userInfo", userInfo);
        } catch (ApiException e) {
            logger.error("获取当前用户信息失败:" + e.getMessage());
            return ServiceResult.failure("获取当前用户信息失败", e.getMessage());
        }
        ServiceResult serviceResult = ServiceResult.success(resultMap);
        logger.info("成功获取当前用户信息");
        return serviceResult;
    }
}
