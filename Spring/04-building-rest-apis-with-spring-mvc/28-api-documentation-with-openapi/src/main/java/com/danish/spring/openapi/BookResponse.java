package com.danish.spring.openapi;

public record BookResponse(Long id, String title, String author, int priceCents) {
}
