package com.sagar.examples.contextAndBeanLifecycle.config;

import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan("com.sagar.examples.contextAndBeanLifecycle")
public class AppConfig {


    public AppConfig(){
        System.out.println("AppConfig instance created");
        System.out.println();
        System.out.println("now ill start creating bean instances");


    }

    @Bean
    public static BeanFactoryPostProcessor beanDefinitionPrinter() {

        return beanFactory -> {


            System.out.println("Scanning completed  ");
            System.out.println("All bean defination is completed");
            System.out.println("Bean factory post processor functional inteface method is called if any bean present of this type");
            System.out.println("in this phase we can modify bean defination iam just printing");
            System.out.println("these are the bean definations below ");

            for (String name : beanFactory.getBeanDefinitionNames()) {

                BeanDefinition bd = beanFactory.getBeanDefinition(name);

                System.out.println("----------------------");
                System.out.println("Bean Name : " + name);
                System.out.println("Bean Class: " + bd.getBeanClassName());
                System.out.println("Scope     : " + bd.getScope());
                System.out.println("Lazy      : " + bd.isLazyInit());
                System.out.println("----------------------");
            }
        };
    }

}
