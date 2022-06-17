package org.jeecg.config;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson.JSONObject;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jeecg.common.constant.CommonConstant;
import org.jeecg.modules.system.entity.SysTenant;
import org.jeecg.modules.system.entity.SysUser;
import org.jeecg.modules.system.model.SysLoginModel;
import org.jeecg.modules.system.service.ISysTenantService;
import org.jeecg.modules.system.service.ISysUserService;
import org.jeecg.modules.system.util.HttpContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;
import java.util.Date;

/**
 *  针对登录接口进行租户登录检查
 *
 * @Author jianghx
 * @Date 2022/3/3 11:26
 * @Version 1.0
 **/
@Component
@Slf4j
@Configuration
public class TenantHandlerInterceptor extends HandlerInterceptorAdapter {

    //yml文件配置登录接口 /sys/login,/sys/mLogin
    @Value("${jeecg.shiro.loginUrlPatterns}")
    private String loginUrlPatterns;

    @Resource
    private ISysTenantService sysTenantService;

    @Resource
    private ISysUserService sysUserService;

    @Resource
    private MyPathMatchingFilterChainResolver myPathMatchingFilterChainResolver;


    /**
     * 针对登录接口进行租户状态、租户时间检查
     * tenantCheckInterceptUrls: /sys/login,/sys/mLogin
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String[] urls = loginUrlPatterns.split(",");
        if(urls.length>0  ){
            String tenantId = request.getHeader(CommonConstant.TENANT_ID);
            //获取当前请求url
            final String requestURI = myPathMatchingFilterChainResolver.getMyPathWithinApplication(request);
            //检查当前请求是否在shirod filterChainDefinitionMap 过滤路径内
            //此处url判断是从滤器源码中复制过来的，与过滤器判断方法保持一致，
            for (String url : urls) {
                if (myPathMatchingFilterChainResolver.MyPathMatches2(url, requestURI) && StringUtils.isNotBlank(tenantId)) {
                    String bodyString = HttpContextUtils.getBodyString(request);

                    if (StringUtils.isNotBlank(loginUrlPatterns) && StringUtils.isNotBlank(bodyString)) {
                        JSONObject jsonObject = JSONObject.parseObject(bodyString);
                        String userName = jsonObject.getString("username");
                        //查询用户
                        SysUser userInfo = sysUserService.getOneUser(userName);

                        //超级用户登录，不进行过期验证
                        if(ObjectUtil.isNotNull(userInfo) && userInfo.getAdminFlag() == 1){
                            return true;
                        }
                        if(ObjectUtil.isNull(userInfo)){
                            throw new NullPointerException("未查询到用户信息！");
                        }

                        SysTenant tenantInfo = sysTenantService.getById(tenantId);
                        // 租户状态状态 1正常 0冻结
                        if(tenantInfo.getStatus() == 1){
                            throw new RuntimeException("租户已冻结,请联系管理员!");
                        }
                        if (null == tenantInfo.getBeginDate() || null == tenantInfo.getEndDate()) {
                            log.error("租户信息", tenantInfo);
                            log.info("===============tenantId=========" + tenantId);
                            log.error("当前租户开始时间、结束时间不能有null存在！");
                            throw new NullPointerException("参数缺失，请联系管理员");
                        }

                        //检查租户到期时间
                        if (null != tenantInfo.getEndDate()) {
                            Date tenantEndDate = tenantInfo.getEndDate();//租户到期时间

                            int val = DateUtil.compare(tenantEndDate, new Date());
                            if (val < 0) {
                                throw new RuntimeException("租户时间已到期！");
                            }
                        }


                    }
                }
            }
        }
        return true;
    }


}

