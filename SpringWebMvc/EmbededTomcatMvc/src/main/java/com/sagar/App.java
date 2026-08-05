package com.sagar;

import com.sagar.config.AppConfig;
import com.sagar.config.WebConfig;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.DispatcherServlet;

import java.io.File;

public class App {
    public static void main(String[] args) throws LifecycleException {
        //creating application context which has service and repositories
        AnnotationConfigApplicationContext appcontext = new AnnotationConfigApplicationContext(AppConfig.class);
        //creating web application context which has controllers and web related beans
        AnnotationConfigWebApplicationContext webContext = new AnnotationConfigWebApplicationContext();
        webContext.register(WebConfig.class);
        //setting parent application context
        webContext.setParent(appcontext);

        //creating dispatcher servlet which has webContext and parent in it appcontext
        DispatcherServlet dispatcherServlet = new DispatcherServlet(webContext);

        //creating tomcat server
        Tomcat tomcat=new Tomcat();
        tomcat.setPort(8080);

        Context context=tomcat.addContext("", new File(".").getAbsolutePath());
        Tomcat.addServlet(context,"dispatcher",dispatcherServlet);
        context.addServletMappingDecoded(
                "/",
                "dispatcher"
        );
        tomcat.getConnector();
        tomcat.start();
        System.out.println("listening on port 8080");
        tomcat.getServer().await();





    }
}
