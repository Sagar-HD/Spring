package com.sagar.FiltersAndInterceptors.config;

import com.sagar.FiltersAndInterceptors.filter.ResponseFilter;
import com.sagar.FiltersAndInterceptors.interceptor.UserInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {


    @Bean
    public FilterRegistrationBean<ResponseFilter> addResponseFilter(){
        FilterRegistrationBean<ResponseFilter> registrationBean = new FilterRegistrationBean<>();
        registrationBean.setFilter(new ResponseFilter());
        System.out.println("registering response filter");

        registrationBean.addUrlPatterns("/users");
        registrationBean.setOrder(3);
        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        UserInterceptor userInterceptor=new UserInterceptor();
        registry.addInterceptor(userInterceptor).addPathPatterns("/users").order(1);


    }




}
