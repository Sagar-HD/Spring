package com.sagar.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(2)
public class LoggingAspect {

    @Before("com.sagar.aspect.ApplicationPointcut.serviceMethods()")
    public void loggingBefore(JoinPoint joinPoint){
        System.out.println("Logging before :"+joinPoint.getSignature().getName());
    }

    @After("com.sagar.aspect.ApplicationPointcut.serviceMethods()")
    public void loggingAfter(JoinPoint joinPoint){
        System.out.println("Logging after :"+joinPoint.getSignature().getName());
    }

    @AfterThrowing(pointcut = "com.sagar.aspect.ApplicationPointcut.serviceMethods()",throwing = "ex")
    public void loggingAfterThrowing(JoinPoint joinPoint,Throwable ex){

        System.out.println("Logging after throwing :"+ex.getMessage());



    }

}
