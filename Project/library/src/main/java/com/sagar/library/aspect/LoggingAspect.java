package com.sagar.library.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.*;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import com.sagar.library.annotation.MeasureTime;

import java.time.LocalDateTime;

@Component
@Aspect
@Slf4j
public class LoggingAspect {



    @Before("execution(* com.sagar.library.service..*(..))")
    public void beforeLog(JoinPoint jp){
        System.out.println("[Before ] "+jp.toShortString());

    }

    @After("execution(* com.sagar.library.service..*(..))")
    public void afterLog(JoinPoint jp){
        System.out.println("[After method completion in finally] "+jp.toShortString());

    }

    @AfterReturning("execution(* com.sagar.library.service..*(..))")
    public void afterReturning(JoinPoint jp){
        System.out.println("[After returning ] "+jp.toShortString());

    }
    @AfterThrowing(pointcut = "execution(* com.sagar.library.service..*(..))",throwing = "ex")
    public void afterReturning(JoinPoint jp,Exception ex){
        System.out.println("[After exception ] "+ex.getClass()+"in method "+jp.toShortString());

    }

    @Around("@annotation(com.sagar.library.annotation.MeasureTime)")
    public Object measureTime(ProceedingJoinPoint jp) throws Throwable {

        long start= System.currentTimeMillis();
        MethodSignature ms=(MethodSignature) jp.getSignature();
        MeasureTime annotation=ms.getMethod().getAnnotation(MeasureTime.class);
        int threshold=annotation.threshold();
        try {
            Object value = jp.proceed();
            return value;
        }
        catch (Throwable e){
            throw e;
        }
        finally {
            long end=System.currentTimeMillis();
            long timeTaken=end-start;
            if(timeTaken>threshold){
                log.warn("Time taken exceeded warning : {} by method {}",timeTaken,jp.getSignature().toShortString());
            }
            else{
                System.out.println("Time taken "+timeTaken);
            }
        }


    }





}
