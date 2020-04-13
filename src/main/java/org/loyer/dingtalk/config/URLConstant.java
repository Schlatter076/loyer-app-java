package org.loyer.dingtalk.config;

public class URLConstant {
    /**
     * 钉钉网关gettoken地址
     */
    public static final String URL_GET_TOKKEN = "https://oapi.dingtalk.com/gettoken";

    /**
     *获取用户在企业内userId的接口URL
     */
    public static final String URL_GET_USER_INFO = "https://oapi.dingtalk.com/user/getuserinfo";

    /**
     *获取用户姓名的接口url
     */
    public static final String URL_USER_GET = "https://oapi.dingtalk.com/user/get";
    /**
     * 获取所有部门列表
     */
    public static final String URL_DEPARTMENT_LIST = "https://oapi.dingtalk.com/department/list";
    /**
     * 根据指定部门查询所有用户
     */
    public static final String URL_USER_LIST = "https://oapi.dingtalk.com/user/simplelist";
    /**
     * 查询用户的公告信息
     */
    public static final String URL_USER_BLACKBOARD = "https://oapi.dingtalk.com/topapi/blackboard/listtopten";
    /**
     * 获取指定时间的日志列表
     */
    public static final String URL_LOG_LIST = "https://oapi.dingtalk.com/topapi/report/list";

}
