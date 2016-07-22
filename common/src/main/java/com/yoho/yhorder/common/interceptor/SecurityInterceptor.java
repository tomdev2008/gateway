package com.yoho.yhorder.common.interceptor;

import com.yoho.yhorder.common.annotation.Security;
import com.yoho.yhorder.common.bean.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.util.WebUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 安全拦截器
 *
 * @author caoqi
 *
 */
public class SecurityInterceptor extends HandlerInterceptorAdapter {

    private final transient Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        User user = (User)WebUtils.getSessionAttribute(request, "loginUser");
        // 控制访问
        if (handler instanceof HandlerMethod) {
            HandlerMethod method = (HandlerMethod) handler;
            Security security = method.getMethodAnnotation(Security.class);
            if (security != null) {

                // 判断会话
                if (WebUtils.getSessionAttribute(request, "loginUser")==null){
                    response.sendRedirect("/invalidSession.jsp");
                    return false;
                }

                String url = security.url();

                logger.debug("url:{}", url);
                return false;
            }
        }
        return true;
    }

}
