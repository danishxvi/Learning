package com.danish.spring.mapping;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.LazyInitializationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MappingService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long createAuthorWithBooks() {
        Author author = new Author("Robert C. Martin");

        Book cleanCode = new Book("Clean Code", Genre.TECHNICAL, new Price(3499, "USD"));
        Book cleanArch = new Book("Clean Architecture", Genre.TECHNICAL, new Price(3999, "USD"));

        Tag softwareEngineering = new Tag("software-engineering");
        entityManager.persist(softwareEngineering);
        cleanCode.addTag(softwareEngineering);
        cleanArch.addTag(softwareEngineering);

        // addBook() keeps BOTH sides in sync in one call (see Author.addBook) -
        // persisting just the author is enough: cascade = ALL on Author.books means
        // Hibernate persists cleanCode and cleanArch automatically, in the same flush.
        author.addBook(cleanCode);
        author.addBook(cleanArch);
        entityManager.persist(author);

        return author.getId();
    }

    // Accessing a LAZY collection WORKS here, because this method itself is
    // @Transactional - the persistence context (and therefore the ability to run the
    // lazy-loading query) is still open for the whole method.
    @Transactional
    public void demonstrateLazyAccessWithinTransaction(Long authorId) {
        Author author = entityManager.find(Author.class, authorId);
        System.out.println("  Author loaded - no query for books yet (LAZY, untouched so far)");
        System.out.println("  Accessing author.getBooks() now:");
        for (Book book : author.getBooks()) {
            System.out.println("    - " + book.getTitle() + " (" + book.getPrice() + ") tags=" + book.getTags());
        }
    }

    // NOT @Transactional. entityManager.find() still works for a direct lookup (each
    // call opens its own tiny implicit transaction), but the returned Book's lazy
    // "author" association is a PROXY, not real data - and by the time this method's
    // second line runs, the persistence context that could have loaded it is long closed.
    public Book fetchBookWithoutTransaction(Long bookId) {
        return entityManager.find(Book.class, bookId);
    }

    public void demonstrateLazyInitializationException(Book book) {
        try {
            System.out.println("  book.getAuthor().getName() = " + book.getAuthor().getName());
        } catch (LazyInitializationException ex) {
            System.out.println("  LazyInitializationException: " + ex.getMessage());
        }
    }

    // orphanRemoval = true in action: removing a Book from the Author's OWN list (not
    // just nulling out book.author) deletes that Book's row entirely, on flush.
    @Transactional
    public void removeOneBook(Long authorId, Long bookId) {
        Author author = entityManager.find(Author.class, authorId);
        Book toRemove = author.getBooks().stream()
                .filter(b -> b.getId().equals(bookId))
                .findFirst()
                .orElseThrow();
        author.removeBook(toRemove); // watch for a DELETE below, with no explicit remove() call on the Book
    }

    // cascade = ALL in action: removing the Author cascades to every REMAINING Book -
    // no need to delete each Book individually first.
    @Transactional
    public void deleteAuthorAndAllBooks(Long authorId) {
        Author author = entityManager.find(Author.class, authorId);
        entityManager.remove(author); // watch for DELETEs for every remaining book, then the author
    }
}
