package com.danish.spring.aspects;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;

import java.util.Arrays;

// @Aspect marks this as a source of advice; @Component is what actually registers it as
// a Spring bean (an @Aspect that is never picked up as a bean does nothing at all - the
// same "declared but not registered" trap as an un-scanned @Configuration).
@Aspect
@Component
public class LoggingAspect {

    // A POINTCUT EXPRESSION, defined once and reused by name below (loggableMethod())
    // instead of repeating the string on every advice method. "execution(...)" matches
    // by method SIGNATURE: any visibility, any return type, any method name, any
    // arguments, on any class inside com.danish.spring.aspects.service (and its
    // sub-packages, via the trailing "..").
    @Pointcut("execution(* com.danish.spring.aspects.service..*(..))")
    public void loggableMethod() {
    }

    // Runs BEFORE the matched method - cannot stop it from running or see its result.
    @Before("loggableMethod()")
    public void logBefore(JoinPoint joinPoint) {
        System.out.println("  [Before] " + signature(joinPoint) + " called with " + Arrays.toString(joinPoint.getArgs()));
    }

    // Runs ONLY if the method returns normally (no exception) - "returning" binds the
    // actual return value into this method's parameter, by matching parameter NAME to
    // the name used in the annotation.
    @AfterReturning(pointcut = "loggableMethod()", returning = "result")
    public void logAfterReturning(JoinPoint joinPoint, Object result) {
        System.out.println("  [AfterReturning] " + signature(joinPoint) + " returned: " + result);
    }

    // Runs ONLY if the method throws - the mirror image of @AfterReturning. Does NOT
    // stop the exception from propagating; it still reaches the original caller after
    // this advice runs.
    @AfterThrowing(pointcut = "loggableMethod()", throwing = "ex")
    public void logAfterThrowing(JoinPoint joinPoint, Throwable ex) {
        System.out.println("  [AfterThrowing] " + signature(joinPoint) + " threw: " + ex.getClass().getSimpleName() + " - " + ex.getMessage());
    }

    // Runs ALWAYS - success or failure - the AOP equivalent of a `finally` block.
    @After("loggableMethod()")
    public void logAfter(JoinPoint joinPoint) {
        System.out.println("  [After] " + signature(joinPoint) + " finished (success or not)");
    }

    private String signature(JoinPoint joinPoint) {
        return joinPoint.getSignature().toShortString();
    }
}
