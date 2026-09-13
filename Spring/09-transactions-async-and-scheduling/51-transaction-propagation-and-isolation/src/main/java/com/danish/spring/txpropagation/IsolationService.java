package com.danish.spring.txpropagation;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class IsolationService {

    @PersistenceContext
    private EntityManager entityManager;

    private final AccountRepository accountRepository;

    public IsolationService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    // @Transactional's "isolation" attribute must be a COMPILE-TIME constant - it
    // cannot be parameterized per call the way propagation was above. Two separate
    // methods, identical bodies, different isolation levels, is the actual mechanism
    // for comparing them (a factored-out private method holds the shared logic).

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.READ_COMMITTED)
    public int[] readTwiceUnderReadCommitted(Long accountId, CountDownLatch firstReadDone, CountDownLatch okToReadAgain)
            throws InterruptedException {
        return readTwice(accountId, firstReadDone, okToReadAgain);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW, isolation = Isolation.REPEATABLE_READ)
    public int[] readTwiceUnderRepeatableRead(Long accountId, CountDownLatch firstReadDone, CountDownLatch okToReadAgain)
            throws InterruptedException {
        return readTwice(accountId, firstReadDone, okToReadAgain);
    }

    private int[] readTwice(Long accountId, CountDownLatch firstReadDone, CountDownLatch okToReadAgain)
            throws InterruptedException {
        int first = accountRepository.findById(accountId).orElseThrow().getBalanceCents();
        firstReadDone.countDown();
        okToReadAgain.await(5, TimeUnit.SECONDS);

        // entityManager.clear() empties the PERSISTENCE CONTEXT (lesson 29's
        // first-level cache) - without this, the second call below would just return
        // the SAME cached Java object from the first read, never actually asking the
        // database again at all, regardless of isolation level.
        entityManager.clear();

        int second = accountRepository.findById(accountId).orElseThrow().getBalanceCents();
        return new int[]{first, second};
    }

    // A SEPARATE, independent transaction that updates and commits immediately - the
    // "other thread" in this lesson's isolation demonstration.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void updateBalanceInOwnTransaction(Long accountId, int newBalance) {
        Account account = accountRepository.findById(accountId).orElseThrow();
        account.setBalanceCents(newBalance);
    }
}
