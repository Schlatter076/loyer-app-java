package org.loyer.dingtalk.util;

import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.request.OapiGettokenRequest;
import com.dingtalk.api.response.OapiGettokenResponse;
import com.taobao.api.ApiException;
import org.loyer.dingtalk.config.Constant;
import org.loyer.dingtalk.config.URLConstant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 获取access_token工具类
 */
public class AccessTokenUtil {
    private static final Logger bizLogger = LoggerFactory.getLogger(AccessTokenUtil.class);

    public static String getToken() throws ApiException {
        try {
            DefaultDingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_GET_TOKKEN);
            OapiGettokenRequest request = new OapiGettokenRequest();

            request.setAppkey(Constant.APP_KEY);
            request.setAppsecret(Constant.APP_SECRET);
            request.setHttpMethod("GET");
            OapiGettokenResponse response = client.execute(request);
            String accessToken = response.getAccessToken();
            return accessToken;
        } catch (ApiException e) {
            bizLogger.error("getAccessToken failed:" + e.getMessage());
            throw e;
        }

    }

    /**
     * 获取指定日期的起始毫秒值和结束毫秒值
     *
     * @param date 待转换的日期-格式为："yyyy-MM-dd"
     * @return long数组
     * @throws ParseException
     */
    public static long[] getOneDayStartAndAfter(String date) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date d = sdf.parse(date);
        long[] res = new long[2];
        res[0] = d.getTime();
        res[1] = res[0] + 24 * 60 * 60 * 1000;
        return res;
    }

    /**
     * 根据指定的毫秒值获取时间字符串(yyyy-MM-dd HH:mm:ss)
     *
     * @param mills
     * @return
     */
    public static String getTimeByMills(long mills) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date d = new Date(mills);
        return sdf.format(d);
    }

}
