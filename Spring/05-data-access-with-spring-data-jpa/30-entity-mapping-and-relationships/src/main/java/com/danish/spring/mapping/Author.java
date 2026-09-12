package com.danish.spring.mapping;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;

@Entity
public class Author {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    // mappedBy = "author" means THIS side does NOT own the relationship - Book.author
    // (with @JoinColumn) does. Author has no foreign key column of its own; "books" here
    // is purely a convenience for navigating from an Author to its Books in Java.
    //
    // cascade = ALL: persisting/removing an Author cascades to its Books automatically.
    // orphanRemoval = true: removing a Book from THIS list (not just clearing the
    // reference) deletes it from the database - see AuthorService for both, demonstrated.
    // fetch defaults to LAZY for @OneToMany - the query for these Books does not run
    // until .getBooks() is actually accessed.
    @OneToMany(mappedBy = "author", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<Book> books = new ArrayList<>();

    protected Author() { }

    public Author(String name) {
        this.name = name;
    }

    // A HELPER METHOD keeping BOTH sides of the bidirectional relationship in sync in
    // one call - forgetting to set book.setAuthor(this) here would leave the Java object
    // graph inconsistent even though the database foreign key ends up correct (Book owns
    // the column) - a classic, easy-to-make bidirectional-relationship bug.
    public void addBook(Book book) {
        books.add(book);
        book.setAuthor(this);
    }

    public void removeBook(Book book) {
        books.remove(book);
        book.setAuthor(null);
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Book> getBooks() { return books; }
}
