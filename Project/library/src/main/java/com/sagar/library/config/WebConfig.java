package com.sagar.library.config;

import com.sagar.library.interceptor.RequestIdInterceptor;

import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableAsync
@EnableScheduling
public class WebConfig implements WebMvcConfigurer {

    private RequestIdInterceptor requestIdInterceptor;

    public WebConfig(RequestIdInterceptor requestIdInterceptor){
        this.requestIdInterceptor=requestIdInterceptor;
    }


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(this.requestIdInterceptor).addPathPatterns("/**");

    }

    @Bean
    public OpenAPI mySwaggerConfig(){
        return new OpenAPI()
                .info(new Info().title("library management").description("spring boot practise app").version("1.O"))
                .components(new Components()
                        .addSecuritySchemes(
                                "bearerAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                        )

                )
                .addSecurityItem(
                        new SecurityRequirement()
                                .addList("bearerAuth")
                );

    }
}
