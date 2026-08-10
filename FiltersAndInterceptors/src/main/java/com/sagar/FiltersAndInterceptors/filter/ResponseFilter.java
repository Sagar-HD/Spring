package com.sagar.FiltersAndInterceptors.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpServletResponseWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

public class ResponseFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);
        filterChain.doFilter(servletRequest,responseWrapper);
        System.out.println("response was :");
        System.out.println(new String(responseWrapper.getContentAsByteArray()));
        responseWrapper.resetBuffer();
        responseWrapper.getWriter().write("hahha u cant see ur response i deleted it");
        responseWrapper.copyBodyToResponse();



    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
