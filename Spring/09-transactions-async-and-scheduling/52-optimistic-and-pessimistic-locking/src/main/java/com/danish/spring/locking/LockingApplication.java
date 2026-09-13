package com.danish.spring.locking;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

// ============================================================================
// 52 - OPTIMISTIC AND PESSIMISTIC LOCKING
// ============================================================================
// Run: mvn -f Spring/09-transactions-async-and-scheduling/52-optimistic-and-pessimistic-locking spring-boot:run
// ============================================================================
@SpringBootApplication
public class LockingApplication {
    public static void main(String[] args) {
        SpringApplication.run(LockingApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final ProductRepository productRepository;
        private final OptimisticLockingService optimisticLockingService;
        private final PessimisticLockingService pessimisticLockingService;

        Demo(ProductRepository productRepository, OptimisticLockingService optimisticLockingService,
             PessimisticLockingService pessimisticLockingService) {
            this.productRepository = productRepository;
            this.optimisticLockingService = optimisticLockingService;
            this.pessimisticLockingService = pessimisticLockingService;
        }

        @Override
        public void run(String... args) throws Exception {
            runOptimisticScenario();
            System.out.println();
            runPessimisticScenario();
        }

        private void runOptimisticScenario() throws Exception {
            System.out.println("=".repeat(74));
            System.out.println("OPTIMISTIC LOCKING - two transactions read the same version, both write");
            System.out.println("=".repeat(74));

            Product product = productRepository.save(new Product("widget", 100));
            System.out.println("  initial: stock=" + product.getStock() + ", version=" + product.getVersion());

            CountDownLatch readDoneA = new CountDownLatch(1);
            CountDownLatch okToUpdateA = new CountDownLatch(1);
            CountDownLatch readDoneB = new CountDownLatch(1);
            CountDownLatch okToUpdateB = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<?> futureA = executor.submit(() -> {
                try {
                    optimisticLockingService.readThenUpdateAfterSignal(product.getId(), 90, readDoneA, okToUpdateA);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            Future<?> futureB = executor.submit(() -> {
                try {
                    optimisticLockingService.readThenUpdateAfterSignal(product.getId(), 80, readDoneB, okToUpdateB);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            readDoneA.await();
            readDoneB.await();
            System.out.println("  both transactions have now read version=" + product.getVersion() + " into memory");

            okToUpdateA.countDown();
            futureA.get();
            System.out.println("  transaction A committed stock=90 - " + productRepository.findById(product.getId()).orElseThrow().getVersion()
                    + " is the new version");

            okToUpdateB.countDown();
            try {
                futureB.get();
                System.out.println("  transaction B committed with NO conflict (unexpected)");
            } catch (ExecutionException ex) {
                Throwable cause = ex.getCause();
                System.out.println("  transaction B failed: " + cause.getClass().getSimpleName() + ": " + cause.getMessage());
            }

            executor.shutdown();
            Product finalState = productRepository.findById(product.getId()).orElseThrow();
            System.out.println("  final: stock=" + finalState.getStock() + ", version=" + finalState.getVersion());
        }

        private void runPessimisticScenario() throws Exception {
            System.out.println("=".repeat(74));
            System.out.println("PESSIMISTIC LOCKING - a second reader BLOCKS until the first commits");
            System.out.println("=".repeat(74));

            Product product = productRepository.save(new Product("gadget", 100));
            System.out.println("  initial: stock=" + product.getStock());

            CountDownLatch lockAcquiredA = new CountDownLatch(1);
            CountDownLatch okToReleaseA = new CountDownLatch(1);
            CountDownLatch lockAcquiredB = new CountDownLatch(1);
            CountDownLatch okToReleaseB = new CountDownLatch(1);

            ExecutorService executor = Executors.newFixedThreadPool(2);
            Future<?> futureA = executor.submit(() -> {
                try {
                    pessimisticLockingService.lockUpdateAndHoldUntilSignalled(product.getId(), 200, lockAcquiredA, okToReleaseA);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });
            lockAcquiredA.await();
            System.out.println("  [main] transaction A holds the PESSIMISTIC_WRITE lock");

            long beforeB = System.currentTimeMillis();
            Future<?> futureB = executor.submit(() -> {
                try {
                    pessimisticLockingService.lockUpdateAndHoldUntilSignalled(product.getId(), 300, lockAcquiredB, okToReleaseB);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            });

            System.out.println("  [main] transaction B submitted - it wants the SAME lock and should now block");
            System.out.println("  [main] holding A's lock for 1500ms before releasing it");
            Thread.sleep(1500);
            okToReleaseA.countDown();
            futureA.get();
            System.out.println("  [main] transaction A committed and released the lock");

            lockAcquiredB.await();
            long blockedForMs = System.currentTimeMillis() - beforeB;
            System.out.println("  [main] transaction B finally acquired the lock after " + blockedForMs + "ms of blocking");
            okToReleaseB.countDown();
            futureB.get();
            executor.shutdown();

            Product finalState = productRepository.findById(product.getId()).orElseThrow();
            System.out.println("  final: stock=" + finalState.getStock() + " (B's write, applied after A's)");
        }
    }
}
