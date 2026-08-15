package com.sagar;

import com.sagar.config.AppConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

public class App {
    public static void main(String[] args) throws LifecycleException {
        AnnotationConfigWebApplicationContext webApplicationContext=new AnnotationConfigWebApplicationContext();
        webApplicationContext.register(AppConfig.class);
        DispatcherServlet dispatcherServlet=new DispatcherServlet(webApplicationContext);
        Tomcat tomcat=new Tomcat();
        tomcat.setPort(8080);
        Context context=tomcat.addContext("",null);
        Tomcat.addServlet(context,"dispatcher",dispatcherServlet);
        context.addServletMappingDecoded("/","dispatcher");
        tomcat.getConnector();
        tomcat.start();
        System.out.println("tomcat started on port 8080");
        tomcat.getServer().await();



    }

}
