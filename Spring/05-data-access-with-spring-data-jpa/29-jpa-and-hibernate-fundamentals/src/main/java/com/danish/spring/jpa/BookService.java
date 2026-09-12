package com.danish.spring.jpa;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Uses EntityManager DIRECTLY - the raw JPA API, with no Spring Data repository
// involved yet (that starts in lesson 31). Seeing this first is what makes Spring Data's
// repositories legible later: they are a layer built ON TOP of exactly this.
@Service
public class BookService {

    // @PersistenceContext injects an EntityManager - JPA's central API for talking to
    // the persistence context (see below). Spring manages its lifecycle automatically.
    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long createBook(String title, String author) {
        Book book = new Book(title, author);
        entityManager.persist(book); // schedules an INSERT - not necessarily sent yet, see lesson 33
        return book.getId();
    }

    // Demonstrates the PERSISTENCE CONTEXT acting as a first-level cache: fetching the
    // SAME id twice, in the SAME transaction, returns the literal same Java object -
    // the second call never even reaches the database.
    @Transactional
    public void demonstrateFirstLevelCache(Long id) {
        Book first = entityManager.find(Book.class, id);
        System.out.println("  First find() - " + (first != null ? "SQL was sent (see the log above this line)" : "not found"));

        Book second = entityManager.find(Book.class, id);
        System.out.println("  Second find() - same id, NO new SQL was sent");
        System.out.println("  first == second (literal same object)? " + (first == second));
    }

    // Demonstrates DIRTY CHECKING: modifying a MANAGED entity's field, with no explicit
    // save/update call at all, still produces a real UPDATE statement - because Hibernate
    // compares the entity's current state against a snapshot taken when it was loaded,
    // at the moment the transaction commits (or the persistence context is flushed).
    @Transactional
    public void demonstrateDirtyChecking(Long id) {
        Book book = entityManager.find(Book.class, id);
        System.out.println("  Loaded book, title=\"" + book.getTitle() + "\"");
        book.setTitle("Effective Java (3rd Edition)"); // no entityManager.persist() or merge() call here
        System.out.println("  Changed title in memory - watch for an UPDATE when this transaction commits");
    }
}
