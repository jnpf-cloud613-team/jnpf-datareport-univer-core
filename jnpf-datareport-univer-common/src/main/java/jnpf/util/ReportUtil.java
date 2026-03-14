package jnpf.util;

import cn.hutool.core.lang.TypeReference;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.Method;
import cn.hutool.json.JSONUtil;
import jnpf.base.ActionResult;
import jnpf.base.UserInfo;
import jnpf.consts.ApiConst;
import jnpf.database.util.TenantDataSourceUtil;
import jnpf.model.login.MeInfoVO;
import jnpf.model.tenant.TenantVO;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.Objects;

/**
 * 报表工具类
 */
@Slf4j
public class ReportUtil {

    public static UserInfo initUserInfo(String token) {
        UserInfo userInfo = UserProvider.getLocalLoginUser();
        if (userInfo == null) {
            try {
                HttpResponse response = HttpRequest.get(ApiConst.ME).header(Constants.AUTHORIZATION, token).execute();
                if (response.isOk()) {
                    String result = response.body();
                    try {
                        ActionResult<MeInfoVO> out = JSONUtil.toBean(result, new TypeReference<ActionResult<MeInfoVO>>() {
                        }, false);
                        if (Objects.equals(out.getCode(), Constants.SUCCESS)) {
                            MeInfoVO meInfoVO = out.getData();
                            if (StringUtil.isNotEmpty(meInfoVO.getUserId())) {
                                // 构造用户信息
                                String tenantId = meInfoVO.getTenantId();
                                userInfo = new UserInfo();
                                userInfo.setId(meInfoVO.getUserId());
                                userInfo.setUserId(meInfoVO.getUserId());
                                userInfo.setUserName(meInfoVO.getUserName());
                                userInfo.setUserAccount(meInfoVO.getUserAccount());
                                userInfo.setTenantId(tenantId);
                                UserProvider.setLocalLoginUser(userInfo);
                                // 设置租户信息
                                if (TenantDataSourceUtil.isMultiTenancy()) {
                                    // 直接从租户系统获取租户信息
                                    TenantVO tenantVO = TenantDataSourceUtil.getRemoteTenantInfo(tenantId);
                                    TenantDataSourceUtil.switchTenant(tenantId, tenantVO);
                                    userInfo.setTenantDbType(tenantVO.getType());
                                    userInfo.setTenantDbConnectionString(tenantVO.getDbName());
                                }
                                return userInfo;
                            }
                            if (log.isDebugEnabled()) {
                                log.debug("获取用户信息: {}", result);
                            }
                        }else{
                            log.error("主系统未成功返回用户信息: {}", out);
                        }
                    } catch (Exception e) {
                        log.error("用户信息解析失败:" + result, e);
                    }
                }
            } catch (Exception e) {
                log.error("获取用户信息错误: {}", e.getMessage());
            }
        }
        return userInfo;
    }

    public static String http(String url, Method method, Map<String, Object> params) {
        HttpRequest request = HttpRequest.of(url).method(method);
        switch (method){
            case GET:
                request.form(params);
                break;
            default:
                request.body(JsonUtil.getObjectToString(params));
                break;
        }
        request.header(Constants.AUTHORIZATION, UserProvider.getToken());
        request.setConnectionTimeout(50000);
        request.setReadTimeout(60000);
        String json = "{}";
        try {
            json = request.execute().body();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return json;
    }
}
