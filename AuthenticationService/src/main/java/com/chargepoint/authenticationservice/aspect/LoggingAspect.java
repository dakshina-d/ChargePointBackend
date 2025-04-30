package com.chargepoint.authenticationservice.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
public class LoggingAspect {

    // Pointcut: all public methods inside com.chargepoint.transactionservice.kafka package
    @Pointcut("execution(public * com.chargepoint..service..*(..))")
    public void kafkaServiceMethods() {
        // Pointcut signature method, empty body
    }

    @Before("kafkaServiceMethods()")
    public void logBeforeMethod(JoinPoint joinPoint) {
        log.info("[START] Method: {} Arguments: {}", joinPoint.getSignature(), joinPoint.getArgs());
    }

    @AfterReturning(pointcut = "kafkaServiceMethods()", returning = "result")
    public void logAfterMethod(JoinPoint joinPoint, Object result) {
        log.info("[END] Method: {} Returned: {}", joinPoint.getSignature(), result);
    }

    @AfterThrowing(pointcut = "kafkaServiceMethods()", throwing = "ex")
    public void logAfterException(JoinPoint joinPoint, Throwable ex) {
        log.error("[EXCEPTION] Method: {} Message: {}", joinPoint.getSignature(), ex.getMessage(), ex);
    }
}