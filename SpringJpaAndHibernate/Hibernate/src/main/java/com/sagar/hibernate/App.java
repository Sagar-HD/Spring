package com.sagar.hibernate;

import com.sagar.hibernate.config.AppConfig;
import jakarta.servlet.Servlet;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class App {
    public static void main(String[] args) throws LifecycleException {
        AnnotationConfigWebApplicationContext context=new AnnotationConfigWebApplicationContext();

    context.register(AppConfig.class);
    //no refresh bcs dispacther servlet does that


        DispatcherServlet dispatcherServlet=new DispatcherServlet(context);


        Tomcat tomcat = new Tomcat();

        tomcat.setPort(8080);

        tomcat.getConnector();

        Context tomcatContext =
                tomcat.addContext("", null);

        Tomcat.addServlet(tomcatContext,"dispatcher",dispatcherServlet);
        tomcatContext
                .addServletMappingDecoded(
                        "/",
                        "dispatcher"
                );


        tomcat.start();

        System.out.println("tomcat listening on port 8080");
        tomcat.getServer().await();


    }
}
