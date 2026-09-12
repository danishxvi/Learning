package com.danish.spring.openapi;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

// @Schema adds human-readable documentation on top of what springdoc can already infer.
// It does NOT replace the Bean Validation annotations (lesson 26) - both are read at the
// same time, from the same fields, and springdoc merges them into ONE generated schema.
public class CreateBookRequest {

    @NotBlank
    @Schema(description = "The book's title", example = "Effective Java")
    private String title;

    @NotBlank
    @Schema(description = "The book's author", example = "Joshua Bloch")
    private String author;

    @Min(0)
    @Schema(description = "Price in cents (never negative)", example = "2999")
    private int priceCents;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public int getPriceCents() { return priceCents; }
    public void setPriceCents(int priceCents) { this.priceCents = priceCents; }
}
