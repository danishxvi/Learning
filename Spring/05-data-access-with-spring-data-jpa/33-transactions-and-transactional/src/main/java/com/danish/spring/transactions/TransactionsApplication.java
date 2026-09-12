package com.danish.spring.transactions;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

// ============================================================================
// 33 - TRANSACTIONS AND @Transactional
// ============================================================================
// Run: mvn -f Spring/05-data-access-with-spring-data-jpa/33-transactions-and-transactional spring-boot:run
// ============================================================================
@SpringBootApplication
public class TransactionsApplication {
    public static void main(String[] args) {
        SpringApplication.run(TransactionsApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final AccountRepository accountRepository;
        private final AuditLogRepository auditLogRepository;
        private final TransferService transferService;

        Demo(AccountRepository accountRepository, AuditLogRepository auditLogRepository, TransferService transferService) {
            this.accountRepository = accountRepository;
            this.auditLogRepository = auditLogRepository;
            this.transferService = transferService;
        }

        @Override
        public void run(String... args) {
            Account alice = accountRepository.save(new Account("Alice", 10_000));
            Account bob = accountRepository.save(new Account("Bob", 5_000));

            System.out.println("=".repeat(74));
            System.out.println("UNCHECKED EXCEPTION - default rollback rule undoes BOTH writes");
            System.out.println("=".repeat(74));
            System.out.println("  Before: Alice=" + balance(alice.getId()) + ", Bob=" + balance(bob.getId()));
            try {
                transferService.transferAndCrash(alice.getId(), bob.getId(), 2_000);
            } catch (RuntimeException ex) {
                System.out.println("  Caught: " + ex.getMessage());
            }
            System.out.println("  After:  Alice=" + balance(alice.getId()) + ", Bob=" + balance(bob.getId())
                    + "  <-- unchanged, rollback happened");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("CHECKED EXCEPTION - default rollback rule does NOT undo the writes");
            System.out.println("=".repeat(74));
            System.out.println("  Before: Alice=" + balance(alice.getId()) + ", Bob=" + balance(bob.getId()));
            try {
                transferService.transferWithCheckedFailureDefaultRollback(alice.getId(), bob.getId(), 2_000);
            } catch (InsufficientFundsException ex) {
                System.out.println("  Caught: " + ex.getMessage());
            }
            System.out.println("  After:  Alice=" + balance(alice.getId()) + ", Bob=" + balance(bob.getId())
                    + "  <-- CHANGED, despite the exception! (the default rollback surprise)");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("SAME CHECKED EXCEPTION, WITH rollbackFor - now it rolls back correctly");
            System.out.println("=".repeat(74));
            System.out.println("  Before: Alice=" + balance(alice.getId()) + ", Bob=" + balance(bob.getId()));
            try {
                transferService.transferWithCheckedFailureExplicitRollback(alice.getId(), bob.getId(), 2_000);
            } catch (InsufficientFundsException ex) {
                System.out.println("  Caught: " + ex.getMessage());
            }
            System.out.println("  After:  Alice=" + balance(alice.getId()) + ", Bob=" + balance(bob.getId())
                    + "  <-- unchanged, rollbackFor fixed it");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("REQUIRES_NEW THROUGH A SEPARATE BEAN - audit entry SURVIVES the rollback");
            System.out.println("=".repeat(74));
            System.out.println("  Audit entries before: " + auditLogRepository.count());
            try {
                transferService.transferWithAuditViaProperBean(alice.getId(), bob.getId(), 2_000);
            } catch (RuntimeException ex) {
                System.out.println("  Caught: " + ex.getMessage());
            }
            System.out.println("  Audit entries after:  " + auditLogRepository.count() + "  <-- the audit entry survived");
            System.out.println("  Balances still unchanged: Alice=" + balance(alice.getId()) + ", Bob=" + balance(bob.getId()));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("REQUIRES_NEW VIA SELF-INVOCATION - the proxy is bypassed, it does NOT survive");
            System.out.println("=".repeat(74));
            System.out.println("  Audit entries before: " + auditLogRepository.count());
            try {
                transferService.transferWithAuditViaSelfInvocation(alice.getId(), bob.getId(), 2_000);
            } catch (RuntimeException ex) {
                System.out.println("  Caught: " + ex.getMessage());
            }
            System.out.println("  Audit entries after:  " + auditLogRepository.count() + "  <-- unchanged - self-invocation defeated REQUIRES_NEW");
        }

        private int balance(Long accountId) {
            return accountRepository.findById(accountId).orElseThrow().getBalanceCents();
        }
    }
}
