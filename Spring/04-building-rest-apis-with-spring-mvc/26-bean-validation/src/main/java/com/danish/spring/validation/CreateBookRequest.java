package com.danish.spring.validation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public class CreateBookRequest {

    @NotBlank(message = "title must not be blank")
    private String title;

    @NotBlank(message = "author must not be blank")
    private String author;

    @Min(value = 0, message = "priceCents must not be negative")
    private int priceCents;

    @ValidIsbn
    private String isbn;

    // @Valid HERE is what makes PublisherInfo's OWN @NotBlank/@Email constraints get
    // checked at all - without it, an invalid nested publisher would pass silently,
    // because validation does not cascade into nested objects automatically.
    @Valid
    private PublisherInfo publisher;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
    public String getIsbn() { return isbn; }
    public void setIsbn(String isbn) { this.isbn = isbn; }
    public PublisherInfo getPublisher() { return publisher; }
    public void setPublisher(PublisherInfo publisher) { this.publisher = publisher; }
}
