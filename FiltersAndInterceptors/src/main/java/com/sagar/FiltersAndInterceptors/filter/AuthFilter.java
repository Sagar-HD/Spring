package com.sagar.FiltersAndInterceptors.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@Order(1)
public class AuthFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
     if(request.getHeader("x-jwt")==null){
         response.getWriter().write("jwt is not present");
         System.out.println("auth filter blocked unauthorised access");
         return;

     }
     filterChain.doFilter(request,response);
    }
}
