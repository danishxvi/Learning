package com.danish.spring.async;

import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// A void @Async method has no return value the caller can inspect and no way to
// propagate an exception back to whoever called it - the call already returned before
// the method body even started running on another thread. Spring's only hook for
// "something went wrong in a fire-and-forget @Async void method" is this handler.
@Component
public class CapturingAsyncExceptionHandler implements AsyncUncaughtExceptionHandler {

    private final List<CapturedAsyncException> captured = new CopyOnWriteArrayList<>();

    @Override
    public void handleUncaughtException(Throwable ex, Method method, Object... params) {
        captured.add(new CapturedAsyncException(method.getName(), ex.getMessage()));
    }

    public List<CapturedAsyncException> getCaptured() {
        return captured;
    }
}
