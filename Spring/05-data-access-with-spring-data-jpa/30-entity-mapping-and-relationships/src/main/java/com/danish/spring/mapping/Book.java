package com.danish.spring.mapping;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
public class Book {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // @Enumerated(STRING) stores the enum's NAME ("TECHNICAL") as text. The alternative,
    // ORDINAL (the default if @Enumerated is omitted), stores its position (0, 1, 2...) -
    // which silently breaks the moment enum constants are reordered or one is inserted
    // in the middle. Always use STRING for anything persisted.
    @Enumerated(EnumType.STRING)
    private Genre genre;

    // @Embedded folds Price's fields directly onto THIS table as plain columns
    // (amount_cents, currency) - no separate table, no foreign key, no join at all.
    @Embedded
    private Price price;

    // THE OWNING side of the Author relationship - @JoinColumn means Book's table gets
    // an author_id foreign key column. fetch = LAZY here is EXPLICIT, because
    // @ManyToOne defaults to EAGER otherwise - the opposite default from @OneToMany.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id")
    private Author author;

    // @ManyToMany needs a JOIN TABLE - neither Book nor Tag's own table can hold this
    // relationship, since either side can relate to many of the other. @JoinTable
    // names that table and its two foreign key columns explicitly.
    @ManyToMany
    @JoinTable(
            name = "book_tags",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id")
    )
    private final Set<Tag> tags = new HashSet<>();

    protected Book() { }

    public Book(String title, Genre genre, Price price) {
        this.title = title;
        this.genre = genre;
        this.price = price;
    }

    public Long getId() { return id; }
    public String getTitle() { return title; }
    public Genre getGenre() { return genre; }
    public Price getPrice() { return price; }
    public Author getAuthor() { return author; }
    void setAuthor(Author author) { this.author = author; } // package-private - only Author's helper methods should call this
    public Set<Tag> getTags() { return tags; }
    public void addTag(Tag tag) { tags.add(tag); }
}
