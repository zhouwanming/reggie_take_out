package com.itheima.reggie.Filter;

import com.alibaba.fastjson.JSON;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 检查用户是否已经完成登录 的过滤器
 */
@Slf4j
@WebFilter(filterName = "loginCheckFilter",urlPatterns = "/*")
public class LoginCheckFilter implements Filter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

        //获取请求URI
        String requestURI = request.getRequestURI();

        log.info("拦截到请求 " + requestURI);

        //定义不需要处理的请求路径
        String[] uris = new String[]{
                "/employee/login",
                "/employee/logout",
                "/backend/**",
                "/front/**"
        };

        //判断本次请求是否需要处理
        boolean check = check(uris, requestURI);

        //若不需要处理，则放行
        if (check){
            log.info("不需要处理");
            filterChain.doFilter(request,response);
            return ;
        }

        //判断是否已登录，如果已登录放行
        if (request.getSession().getAttribute("employee") !=null){
            Long empId = (Long)request.getSession().getAttribute("employee");
            BaseContext.setThreadLocal(empId);
            log.info("已登录 -> " + empId);
            filterChain.doFilter(request,response);
            return ;
        }

        log.info("用户未登录");
        //如果未登录则返回未登录页面 , 通过输出流向客户端响应数据
        response.getWriter().write(JSON.toJSONString(R.error("NOTLOGIN")));
        return;
    }

    /**
     * 路径匹配，检查本次请求是否需要放行
     * @param uris
     * @param requestURI
     * @return
     */
    public boolean check(String[] uris, String requestURI){

        for (String s : uris) {
            boolean match = PATH_MATCHER.match(s, requestURI);
            if (match){
                return true;
            }
        }
        return false;
    }
}
