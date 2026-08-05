package com.sagar.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = {"com.sagar.service","com.sagar.repository"})
public class AppConfig {

}
