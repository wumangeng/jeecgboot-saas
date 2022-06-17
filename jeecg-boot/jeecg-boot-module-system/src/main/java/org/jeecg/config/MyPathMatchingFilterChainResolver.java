package org.jeecg.config;

import org.apache.shiro.util.PatternMatcher;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;

@Component
public class MyPathMatchingFilterChainResolver extends PathMatchingFilterChainResolver {


    public String  getMyPathWithinApplication(ServletRequest request){
        return super.getPathWithinApplication(request);
    }

    public boolean MyPathMatches2(String pattern, String path) {
        return super.pathMatches(pattern,path);
    }
}

