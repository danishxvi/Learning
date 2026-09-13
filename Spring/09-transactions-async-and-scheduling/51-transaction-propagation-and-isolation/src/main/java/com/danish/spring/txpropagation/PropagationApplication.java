package com.danish.spring.txpropagation;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.transaction.IllegalTransactionStateException;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

// ============================================================================
// 51 - TRANSACTION PROPAGATION AND ISOLATION
// ============================================================================
// Run: mvn -f Spring/09-transactions-async-and-scheduling/51-transaction-propagation-and-isolation spring-boot:run
// ============================================================================
@SpringBootApplication
public class PropagationApplication {
    public static void main(String[] args) {
        SpringApplication.run(PropagationApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final AccountRepository accountRepository;
        private final AuditLogRepository auditLogRepository;
        private final PropagationService propagationService;
        private final AuditService auditService;
        private final PlainCaller plainCaller;
        private final IsolationService isolationService;

        Demo(AccountRepository accountRepository, AuditLogRepository auditLogRepository,
             PropagationService propagationService, AuditService auditService,
             PlainCaller plainCaller, IsolationService isolationService) {
            this.accountRepository = accountRepository;
            this.auditLogRepository = auditLogRepository;
            this.propagationService = propagationService;
            this.auditService = auditService;
            this.plainCaller = plainCaller;
            this.isolationService = isolationService;
        }

        @Override
        public void run(String... args) throws Exception {
            Account account = accountRepository.save(new Account("bob", 10_000));

            System.out.println("=".repeat(74));
            System.out.println("NESTED - intended: a failing savepoint should NOT undo the outer transaction");
            System.out.println("=".repeat(74));
            propagationService.updateBalanceWithNestedAudit(account.getId(), 9_000, true);
            System.out.println("  Balance after (outer survives, but not via a savepoint - see below): "
                    + accountRepository.findById(account.getId()).orElseThrow().getBalanceCents());
            System.out.println("  Audit log entries after (NESTED never ran at all): " + auditLogRepository.count());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("NESTED - intended: a succeeding savepoint should commit with the outer tx");
            System.out.println("=".repeat(74));
            propagationService.updateBalanceWithNestedAudit(account.getId(), 8_000, false);
            System.out.println("  Balance after: " + accountRepository.findById(account.getId()).orElseThrow().getBalanceCents());
            System.out.println("  Audit log entries after (still never ran - same rejection either way): " + auditLogRepository.count());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("MANDATORY - called with NO transaction active");
            System.out.println("=".repeat(74));
            try {
                plainCaller.callMandatoryWithNoTransaction();
            } catch (IllegalTransactionStateException ex) {
                System.out.println("  IllegalTransactionStateException: " + ex.getMessage());
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("NEVER - called FROM WITHIN an active transaction");
            System.out.println("=".repeat(74));
            try {
                propagationService.callNeverPropagationFromInsideATransaction();
            } catch (IllegalTransactionStateException ex) {
                System.out.println("  IllegalTransactionStateException: " + ex.getMessage());
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("ISOLATION - READ_COMMITTED: does a second read see a concurrent commit?");
            System.out.println("=".repeat(74));
            runIsolationScenario(account.getId(), true);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("ISOLATION - REPEATABLE_READ: does a second read see a concurrent commit?");
            System.out.println("=".repeat(74));
            runIsolationScenario(account.getId(), false);
        }

        private void runIsolationScenario(Long accountId, boolean readCommitted) throws Exception {
            CountDownLatch firstReadDone = new CountDownLatch(1);
            CountDownLatch okToReadAgain = new CountDownLatch(1);
            ExecutorService executor = Executors.newSingleThreadExecutor();

            Future<int[]> future = executor.submit(() -> readCommitted
                    ? isolationService.readTwiceUnderReadCommitted(accountId, firstReadDone, okToReadAgain)
                    : isolationService.readTwiceUnderRepeatableRead(accountId, firstReadDone, okToReadAgain));

            int updatedTo = readCommitted ? 7_500 : 6_000;
            firstReadDone.await();
            System.out.println("  [main thread] first read finished - now updating and committing from ANOTHER transaction");
            isolationService.updateBalanceInOwnTransaction(accountId, updatedTo);
            System.out.println("  [main thread] update committed - releasing the reading transaction to read again");
            okToReadAgain.countDown();

            int[] results = future.get();
            executor.shutdown();

            System.out.println("  first read=" + results[0] + ", second read=" + results[1]
                    + (results[0] == results[1] ? "  (SAME - isolation held)" : "  (DIFFERENT - non-repeatable read)"));
        }
    }
}
