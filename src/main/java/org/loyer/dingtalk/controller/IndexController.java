package org.loyer.dingtalk.controller;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiBlackboardListtoptenRequest;
import com.dingtalk.api.request.OapiUserGetuserinfoRequest;
import com.dingtalk.api.response.OapiBlackboardListtoptenResponse;
import com.dingtalk.api.response.OapiUserGetuserinfoResponse;
import com.taobao.api.ApiException;
import org.loyer.dingtalk.config.URLConstant;
import org.loyer.dingtalk.util.AccessTokenUtil;
import org.loyer.dingtalk.util.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
public class IndexController {

    private static final Logger logger = LoggerFactory.getLogger(ManagerController.class);

    /**
     * 欢迎页面,通过url访问，判断后端服务是否启动
     */
    @RequestMapping(value = "/welcome", method = RequestMethod.GET)
    public String welcome() {
        return "welcome";
    }

    /**
     * 获取当前用户的公告信息
     * @param requestAuthCode
     * @return
     */
    @RequestMapping(value = "/announcement", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResult login(@RequestParam(value = "authCode") String requestAuthCode) {

        Map<String, Object> resultMap = new HashMap<>();
        logger.info("获取当前用户的公告信息");
        try {
            //获取accessToken
            String accessToken = AccessTokenUtil.getToken();
            logger.info("accessToken = " + accessToken);
            //获取用户userID
            DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_GET_USER_INFO);
            OapiUserGetuserinfoRequest request = new OapiUserGetuserinfoRequest();
            request.setCode(requestAuthCode);
            request.setHttpMethod("GET");
            OapiUserGetuserinfoResponse response = client.execute(request, accessToken);
            String userId = response.getUserid();
            //添加用户userId
            resultMap.put("userId", userId);

            DingTalkClient c2 = new DefaultDingTalkClient(URLConstant.URL_USER_BLACKBOARD);
            OapiBlackboardListtoptenRequest req = new OapiBlackboardListtoptenRequest();
            req.setUserid(userId);
            OapiBlackboardListtoptenResponse rsp = c2.execute(req, accessToken);
            List<OapiBlackboardListtoptenResponse.OapiBlackboardVo> list = rsp.getBlackboardList();
            //添加公告信息
            resultMap.put("announcementList", list);
        } catch (ApiException e) {
            logger.error("获取当前用户公告信息失败:" + e.getMessage());
            return ServiceResult.failure("获取当前用户公告信息失败", e.getMessage());
        }
        ServiceResult serviceResult = ServiceResult.success(resultMap);
        logger.info("成功获取当前用户公告信息");
        return serviceResult;
    }
}
