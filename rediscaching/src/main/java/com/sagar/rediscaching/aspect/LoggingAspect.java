package com.sagar.rediscaching.aspect;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Component
@Aspect
public class LoggingAspect {

    @Around("execution(* com.sagar.rediscaching.service.JokesService.getAllJokes(..))")
    public Object measureTime(ProceedingJoinPoint jp) {
        long start = System.currentTimeMillis();

        try {
           return jp.proceed();
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
        finally {
            long end=System.currentTimeMillis();
            long timeTaken=end-start;
            System.out.println("time taken : "+timeTaken);

        }

    }

}
