package org.jeecg.config;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;


@Component
public class FilterConfiguration {

    /**
     * 注册过滤器bean MyHttpServletRequestFilter
     *  针对于登录接口  /sys/login、/sys/mLogin
     *  登录接口在拦截器(TenantHandlerInterceptor)中会取出request中的body参数
     * @return
     */
    @Bean
    public FilterRegistrationBean MyHttpServletRequestFilter() {

        FilterRegistrationBean registration = new FilterRegistrationBean();
        registration.setFilter(new MyHttpServletRequestFilter());
        //登录接口 url ,具体url 根据项目进行相应修改
        registration.addUrlPatterns("/sys/login","/sys/mLogin");

        registration.setName("MyHttpServletRequestFilter");
        registration.setOrder(1000);
        return registration;
    }

}


