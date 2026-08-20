package com.sagar.springoauth2.config;

import com.sagar.springoauth2.security.CustomOidcUserService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
   public SecurityFilterChain securityFilterChain(HttpSecurity http, CustomOidcUserService customOidcUserService){

        http
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/", "/login**", "/error").permitAll()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth -> oauth
                        .userInfoEndpoint(userInfo -> userInfo
                                .oidcUserService(customOidcUserService)
                        )
                        .defaultSuccessUrl("/profile", true)
                );

        return http.build();
    }
}
