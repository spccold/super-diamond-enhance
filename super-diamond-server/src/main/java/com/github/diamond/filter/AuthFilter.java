package com.github.diamond.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.common.primitives.Ints;


/**
 *
 * @author coffee
 *         create by 2015年3月12日 下午2:44:44
 */
public class AuthFilter implements Filter {

    private static final String COOKIE_A = "2a12a919c703759368ad5b456dafa8c6";

    private static final String COOKIE_B = "y002gQnTsNYmqJebtepQyeIeDNWL0oSqVCTs0YAEPYFhp2YfV5HBs";


    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
     */
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }


    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest, javax.servlet.ServletResponse, javax.servlet.FilterChain)
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;
        Cookie[] cookies = req.getCookies();
        int a = 0;
        int b = 0;
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                switch (cookie.getName()) {
                case COOKIE_A:
                    a = Ints.tryParse(cookie.getValue());
                    break;
                case COOKIE_B:
                    b = Ints.tryParse(cookie.getValue());
                    break;
                default:
                    break;
                }
            }
        }
        if (a == b || a * 51 != b) {
            resp.sendRedirect("http://monitor.51zhangdan.com/manageMonitor/power/login.htm?redirectURL=superdiamond");
            return;
        }
        chain.doFilter(request, response);

    }


    /*
     * (non-Javadoc)
     * @see javax.servlet.Filter#destroy()
     */
    @Override
    public void destroy() {

    }

}
