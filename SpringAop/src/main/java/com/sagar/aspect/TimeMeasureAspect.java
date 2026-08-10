package com.sagar.aspect;

import com.sagar.annotation.MeasureTime;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Aspect
@Order(1)
public class TimeMeasureAspect {
    public TimeMeasureAspect() {
        super();
        System.out.println("TimeMeasureAspect" + " is created");
    }
    @Around("@annotation(measureTime)")
    public Object timeMeasure(ProceedingJoinPoint joinPoint, MeasureTime measureTime) throws Throwable {
        long startTime = System.currentTimeMillis();
try{
    return joinPoint.proceed();
}
finally {
    long endTime = System.currentTimeMillis();
    long duration = endTime - startTime;
    if(duration > measureTime.warningTime()){
        System.out.println("Warning :"+"Method "+measureTime.name()+" took "+duration+"ms");
    }
    else{
        System.out.println("Method "+measureTime.name()+" took "+duration+"ms");
    }

}


    }
}
