package com.danish.spring.aspects;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class TimingAspect {

    // @Around is the most powerful advice type: it receives a ProceedingJoinPoint and
    // decides WHETHER and WHEN to actually call the real method (proceed()) - unlike
    // @Before/@After/@AfterReturning, which can only observe, @Around can skip the call
    // entirely, call it multiple times, catch and swallow its exception, or - as here -
    // wrap it with timing that measures the REAL method body, not just the advice.
    //
    // "@annotation(Timed)" matches any method carrying the @Timed annotation, in any
    // class, regardless of package - a completely different matching STYLE from
    // LoggingAspect's execution() pointcut, which matches by package/class/signature.
    @Around("@annotation(Timed)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long start = System.nanoTime();
        Object result = joinPoint.proceed(); // the ACTUAL method body runs here
        long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
        System.out.println("  [Timed] " + joinPoint.getSignature().toShortString() + " took " + elapsedMillis + " ms");
        return result;
    }
}
