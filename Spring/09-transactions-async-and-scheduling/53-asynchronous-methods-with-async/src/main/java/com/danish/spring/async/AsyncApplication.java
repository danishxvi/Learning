package com.danish.spring.async;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

// ============================================================================
// 53 - ASYNCHRONOUS METHODS WITH @Async
// ============================================================================
// Run: mvn -f Spring/09-transactions-async-and-scheduling/53-asynchronous-methods-with-async spring-boot:run
// ============================================================================
@SpringBootApplication
public class AsyncApplication {
    public static void main(String[] args) {
        SpringApplication.run(AsyncApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final NotificationService notificationService;
        private final SelfInvokingService selfInvokingService;
        private final CapturingAsyncExceptionHandler capturingAsyncExceptionHandler;
        private final ConfigurableApplicationContext context;

        Demo(NotificationService notificationService, SelfInvokingService selfInvokingService,
             CapturingAsyncExceptionHandler capturingAsyncExceptionHandler, ConfigurableApplicationContext context) {
            this.notificationService = notificationService;
            this.selfInvokingService = selfInvokingService;
            this.capturingAsyncExceptionHandler = capturingAsyncExceptionHandler;
            this.context = context;
        }

        @Override
        public void run(String... args) throws Exception {
            runParallelExecutionScenario();
            System.out.println();
            runUncaughtExceptionScenario();
            System.out.println();
            runSelfInvocationScenario();

            // The ThreadPoolTaskExecutor bean owns non-daemon threads that would
            // otherwise keep the JVM alive forever after this method returns. Closing
            // the context runs its shutdown (DisposableBean), but Maven's spring-boot:run
            // fork still doesn't notice the JVM is otherwise idle - an explicit exit is
            // what actually ends the process for this demo.
            context.close();
            System.exit(0);
        }

        private void runParallelExecutionScenario() throws Exception {
            System.out.println("=".repeat(74));
            System.out.println("@Async runs on a DIFFERENT thread, and calls genuinely overlap in time");
            System.out.println("=".repeat(74));
            System.out.println("  [main] calling thread is " + Thread.currentThread().getName());

            long start = System.currentTimeMillis();
            CompletableFuture<String> f1 = notificationService.sendEmailAsync("alice@example.com", 1000);
            CompletableFuture<String> f2 = notificationService.sendEmailAsync("bob@example.com", 1000);
            CompletableFuture<String> f3 = notificationService.sendEmailAsync("carol@example.com", 1000);
            System.out.println("  [main] all three calls returned immediately - elapsed so far: "
                    + (System.currentTimeMillis() - start) + "ms");

            CompletableFuture.allOf(f1, f2, f3).get(5, TimeUnit.SECONDS);
            long elapsed = System.currentTimeMillis() - start;
            for (CompletableFuture<String> f : List.of(f1, f2, f3)) {
                System.out.println("  " + f.get());
            }
            System.out.println("  total elapsed: " + elapsed + "ms for three 1000ms tasks"
                    + (elapsed < 2000 ? "  (ran IN PARALLEL, not sequentially)" : "  (unexpectedly sequential)"));
        }

        private void runUncaughtExceptionScenario() throws Exception {
            System.out.println("=".repeat(74));
            System.out.println("a void @Async method's exception has nowhere to go but the handler");
            System.out.println("=".repeat(74));
            notificationService.sendEmailFireAndForget("dave@example.com");
            System.out.println("  [main] the call above already returned - no exception was thrown here");

            Thread.sleep(500);
            System.out.println("  handler captured " + capturingAsyncExceptionHandler.getCaptured().size() + " exception(s):");
            for (CapturedAsyncException captured : capturingAsyncExceptionHandler.getCaptured()) {
                System.out.println("    " + captured.methodName() + " -> " + captured.message());
            }
        }

        private void runSelfInvocationScenario() throws Exception {
            System.out.println("=".repeat(74));
            System.out.println("self-invocation bypasses the @Async proxy entirely");
            System.out.println("=".repeat(74));
            String mainThreadName = Thread.currentThread().getName();
            String asyncMethodThreadName = selfInvokingService.callAsyncMethodViaThis();
            System.out.println("  [main] calling thread:              " + mainThreadName);
            System.out.println("  \"async\" method actually ran on:     " + asyncMethodThreadName);
            System.out.println("  " + (mainThreadName.equals(asyncMethodThreadName)
                    ? "SAME thread - @Async was silently ignored due to self-invocation"
                    : "DIFFERENT thread - unexpected"));
        }
    }
}
