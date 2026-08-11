package com.sagar.ticketbooking;

import com.sagar.ticketbooking.config.AppConfig;
import com.sagar.ticketbooking.config.WebConfig;
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
        ApplicationContext rootContext=new AnnotationConfigApplicationContext(AppConfig.class);

        AnnotationConfigWebApplicationContext webContext=new AnnotationConfigWebApplicationContext();
        webContext.setParent(rootContext);
        webContext.register(WebConfig.class);
        DispatcherServlet dispatcherServlet=new DispatcherServlet(webContext);


        Tomcat tomcat=new Tomcat();
        tomcat.setPort(8080);

        //created a tomcat context with base folder as current folder
        Context tomcatContext= tomcat.addContext("", new File(".").getAbsolutePath());


        //register dispatcher servlet
        Tomcat.addServlet(tomcatContext,"frontController",dispatcherServlet);

        //map dispatcher servlet to root url
        tomcatContext.addServletMappingDecoded("/", "frontController");

        //most important to this creates a connector with port and registers it to tomcat
        tomcat.getConnector();

        //now ill start tomcat
        tomcat.start();
        System.out.println("tomcat started on port 8080");

        //keep the main thread alive
        tomcat.getServer().await();




    }
}
