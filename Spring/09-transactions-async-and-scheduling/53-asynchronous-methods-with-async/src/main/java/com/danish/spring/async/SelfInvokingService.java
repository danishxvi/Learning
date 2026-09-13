package com.danish.spring.async;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class SelfInvokingService {

    @Async
    public CompletableFuture<String> asyncMethod() {
        return CompletableFuture.completedFuture(Thread.currentThread().getName());
    }

    // @Async (like @Transactional - lesson 38's proxy problem) only works when the call
    // arrives THROUGH the Spring proxy wrapping this bean. Calling asyncMethod() as
    // "this.asyncMethod()" from inside the same class bypasses the proxy entirely - it's
    // a plain Java method call, so it runs synchronously on the CALLER's thread despite
    // the @Async annotation sitting right there on the method.
    public String callAsyncMethodViaThis() throws ExecutionException, InterruptedException {
        return asyncMethod().get();
    }
}
