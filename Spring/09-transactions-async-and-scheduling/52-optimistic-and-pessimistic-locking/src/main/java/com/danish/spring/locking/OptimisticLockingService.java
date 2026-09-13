package com.danish.spring.locking;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

@Service
public class OptimisticLockingService {

    private final ProductRepository productRepository;

    public OptimisticLockingService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Reads the product (capturing whatever @Version value is current), waits for a
    // signal, THEN writes. The version check happens on the UPDATE statement Hibernate
    // issues when this transaction commits and flushes - not at read time and not at
    // setStock() time. Both callers below read the SAME version before either commits,
    // which is exactly the race @Version exists to catch.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void readThenUpdateAfterSignal(Long productId, int newStock, CountDownLatch readDone, CountDownLatch okToUpdate)
            throws InterruptedException {
        Product product = productRepository.findById(productId).orElseThrow();
        readDone.countDown();
        okToUpdate.await(5, TimeUnit.SECONDS);
        product.setStock(newStock);
        // No explicit save() call needed - "product" is a managed entity in this
        // transaction's persistence context; Hibernate's dirty checking (lesson 29)
        // flushes the change, including the versioned UPDATE, at commit time.
    }
}
