package com.danish.spring.locking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class PessimisticLockingService {

    private final ProductRepository productRepository;

    public PessimisticLockingService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Acquires the row lock (blocking here IS the point - a second caller trying this
    // same method on the same id parks until this transaction commits), signals that
    // the lock is held, then waits for a signal before writing and returning - which is
    // what actually releases the lock, since PESSIMISTIC_WRITE is held until commit.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void lockUpdateAndHoldUntilSignalled(Long productId, int newStock, CountDownLatch lockAcquired, CountDownLatch okToRelease)
            throws InterruptedException {
        Product product = productRepository.findByIdForUpdate(productId).orElseThrow();
        lockAcquired.countDown();
        okToRelease.await(5, TimeUnit.SECONDS);
        product.setStock(newStock);
    }
}
