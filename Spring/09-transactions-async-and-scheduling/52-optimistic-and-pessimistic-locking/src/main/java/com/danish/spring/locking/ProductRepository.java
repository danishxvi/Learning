package com.danish.spring.locking;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    // PESSIMISTIC_WRITE translates to "SELECT ... FOR UPDATE" on databases that support
    // it - it takes a real row-level lock at read time, held until the transaction
    // commits or rolls back. Any other transaction trying to acquire the SAME lock
    // (via this same method) blocks until this one releases it.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Product p where p.id = :id")
    Optional<Product> findByIdForUpdate(@Param("id") Long id);
}
