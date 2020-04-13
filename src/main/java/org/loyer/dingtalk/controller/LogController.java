package org.loyer.dingtalk.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.dingtalk.api.DefaultDingTalkClient;
import com.dingtalk.api.DingTalkClient;
import com.dingtalk.api.request.OapiDepartmentListRequest;
import com.dingtalk.api.request.OapiReportListRequest;
import com.dingtalk.api.request.OapiUserSimplelistRequest;
import com.dingtalk.api.response.OapiDepartmentListResponse;
import com.dingtalk.api.response.OapiReportListResponse;
import com.dingtalk.api.response.OapiUserSimplelistResponse;
import com.taobao.api.ApiException;
import org.loyer.dingtalk.config.URLConstant;
import org.loyer.dingtalk.util.AccessTokenUtil;
import org.loyer.dingtalk.util.ServiceResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class LogController {

    private static final Logger logger = LoggerFactory.getLogger(LogController.class);

    @RequestMapping(value = "/allStaff", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResult getAllStaff() {
        logger.info("获取全部员工");
        Map<String, Object> resMap = new HashMap<>();

        try {
            //获取accessToken
            String accessToken = AccessTokenUtil.getToken();
            logger.info("accessToken = " + accessToken);
            logger.info("获取部门列表");
            DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_DEPARTMENT_LIST);
            OapiDepartmentListRequest req = new OapiDepartmentListRequest();
            req.setHttpMethod("GET");
            OapiDepartmentListResponse rsp = client.execute(req, accessToken);
            List<OapiDepartmentListResponse.Department> departments = rsp.getDepartment();
            logger.info("根据部门id获取员工信息");
            DingTalkClient client2 = new DefaultDingTalkClient(URLConstant.URL_USER_LIST);
            OapiUserSimplelistRequest req2 = new OapiUserSimplelistRequest();
            Set<String> nameList = new HashSet<>();
            Map<String, String> userMap = new HashMap<>();
            for(OapiDepartmentListResponse.Department department : departments) {
                req2.setDepartmentId(department.getId());
                req2.setHttpMethod("GET");
                OapiUserSimplelistResponse rsp2 = client2.execute(req2, accessToken);
                JSONObject jo = JSON.parseObject(rsp2.getBody());
                JSONArray ja = jo.getJSONArray("userlist");
                for(Object o : ja) {
                    JSONObject jo1 = JSON.parseObject(o.toString());
                    String name = jo1.getString("name");
                    String id = jo1.getString("userid");
                    nameList.add(name);
                    userMap.put(name, id);
                }
            }
            resMap.put("nameList", nameList);
            resMap.put("userMap", userMap);
        } catch (ApiException e) {
            logger.error("获取部门列表失败::" + e.getMessage());
            return ServiceResult.failure("获取部门列表失败", e.getMessage());
        }
        ServiceResult res = ServiceResult.success(resMap);
        logger.info("成功获取所有员工信息");
        return res;
    }

    @RequestMapping(value = "/userLogDetails", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResult getUserLogDetails(@RequestParam(value = "userId")String userId,
                                            @RequestParam(value = "date")String date) {

        boolean hasMore;
        long cursor = 0L;
        Map<String, Object> map = new HashMap<>();
        logger.info("开始获取当前用户日志详情");
        try {
            //获取accessToken
            String accessToken = AccessTokenUtil.getToken();
            logger.info("accessToken = " + accessToken);
            DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_LOG_LIST);
            OapiReportListRequest request = new OapiReportListRequest();
            long[] ts = AccessTokenUtil.getOneDayStartAndAfter(date);
            request.setStartTime(ts[0]);
            request.setEndTime(ts[1]);
            request.setSize(20L);
            request.setUserid(userId);

            List<String> createTimeList = new ArrayList<>();

            do {
                request.setCursor(cursor);
                OapiReportListResponse response = client.execute(request, accessToken);
//                System.out.println(response.getBody());
                JSONObject jo = JSON.parseObject(response.getBody()).getJSONObject("result");
                hasMore = jo.getBoolean("has_more");
                cursor = jo.getLong("next_cursor");
                JSONArray dataArr = jo.getJSONArray("data_list");

                //取详情
                for(int i = 0; i < dataArr.size(); i++) {
                    JSONObject jobj = dataArr.getJSONObject(i);
                    //取出创建时间
                    String ct = AccessTokenUtil.getTimeByMills(jobj.getLong("create_time"));
                    createTimeList.add(ct);
                    //创建每一条日志详情
                    Map<String, Object> detailsMap = new HashMap<>();
                    //获取图片列表
                    List<String> imgList = new ArrayList<>();
                    JSONArray imgArr = jobj.getJSONArray("images");
                    for(int j = 0; j < imgArr.size(); j++) {
                        JSONObject imgO = JSON.parseObject(imgArr.getString(j));
                        imgList.add(imgO.getString("image"));
                    }
                    detailsMap.put("imgList", imgList);
                    detailsMap.put("templateName", jobj.getString("template_name"));
                    detailsMap.put("reportId", jobj.getString("report_id"));
                    detailsMap.put("deptName", jobj.getString("dept_name"));
                    detailsMap.put("creatorName", jobj.getString("creator_name"));
                    detailsMap.put("createTime", ct);
                    detailsMap.put("contents", jobj.getJSONArray("contents"));
                    //将明细添加进map
                    map.put(ct, detailsMap);
                }

            } while (hasMore);

            map.put("createTimeList", createTimeList);
        } catch (Exception e) {
            logger.warn("获取当前用户日志详情失败:" + e.getMessage());
            return ServiceResult.failure("获取当前用户日志详情失败", e.getMessage());
        }
        ServiceResult res = ServiceResult.success(map);
        logger.info("成功获取当前用户日志详情");
        return res;
    }
    @RequestMapping(value = "/logAnalysis", method = RequestMethod.POST)
    @ResponseBody
    public ServiceResult getLogDataAnalysis(@RequestParam(value = "currentTime")String date) {

        boolean hasMore;
        long cursor = 0L;
        Map<String, Object> map = new HashMap<>();
        logger.info("开始获取当前日期日志提交情况");
        try {
            //获取accessToken
            String accessToken = AccessTokenUtil.getToken();
            logger.info("accessToken = " + accessToken);
            DingTalkClient client = new DefaultDingTalkClient(URLConstant.URL_LOG_LIST);
            OapiReportListRequest request = new OapiReportListRequest();
            long[] ts = AccessTokenUtil.getOneDayStartAndAfter(date);
            request.setStartTime(ts[0]);
            request.setEndTime(ts[1]);
            request.setSize(20L);

            List<String> temList = new ArrayList<>();
            List<String> categories = new ArrayList<>();
            List<Integer> seriesData = new ArrayList<>();

            do {
                request.setCursor(cursor);
                OapiReportListResponse response = client.execute(request, accessToken);
                JSONObject jo = JSON.parseObject(response.getBody()).getJSONObject("result");
                hasMore = jo.getBoolean("has_more");
                cursor = jo.getLong("next_cursor");

                JSONArray dataArr = jo.getJSONArray("data_list");

                //取详情
                for(int i = 0; i < dataArr.size(); i++) {
                    String name = dataArr.getJSONObject(i).getString("creator_name");
                    if(!categories.contains(name)) { //如果不包含，则添加
                        categories.add(name);
                    }
                    temList.add(name);
                }

            } while (hasMore);
            //统计日志创建者出现的次数
            for(String n : categories) {
                //将次数添加
                seriesData.add(Collections.frequency(temList, n));
            }
            map.put("categories", categories);
            map.put("seriesData", seriesData);
            map.put("tickCount", categories.size());

        } catch (Exception e) {
            logger.warn("获取当前日期日志提交情况失败:" + e.getMessage());
            return ServiceResult.failure("获取当前日期日志提交情况失败:", e.getMessage());
        }
        ServiceResult res = ServiceResult.success(map);
        logger.info("成功获取当前日期日志提交情况");
        return res;
    }
}
