package com.sagar.aspect;

import org.aspectj.lang.annotation.Pointcut;

public class ApplicationPointcut {

    @Pointcut("execution(* com.sagar.service.*.*(..))")
    public void serviceMethods() {
    }


}
