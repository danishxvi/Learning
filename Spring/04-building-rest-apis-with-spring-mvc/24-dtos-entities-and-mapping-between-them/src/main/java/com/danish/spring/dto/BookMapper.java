package com.danish.spring.dto;

import org.springframework.stereotype.Component;

import java.time.Instant;

// The mapping code, written by hand and kept in ONE place. Every conversion between the
// entity shape and either DTO shape goes through here - a controller never touches
// Book's constructor or BookResponse's constructor directly for this purpose.
//
// A note on tooling: real projects with many entities often reach for MapStruct, a
// compile-time code generator that writes exactly this kind of method for you from an
// interface you declare. It is a legitimate and common choice - it is not used here
// because it needs its own annotation processor wired into the build, and seeing the
// mapping written out by hand once is worth more, pedagogically, than generating it
// invisibly. Once this shape feels obvious, MapStruct is a reasonable next tool to learn.
@Component
public class BookMapper {

    public BookResponse toResponse(Book book) {
        return new BookResponse(book.getId(), book.getTitle(), book.getAuthor(), book.getCreatedAt());
    }

    public Book toEntity(CreateBookRequest request) {
        // id is null - the repository assigns it. createdAt is set HERE, not accepted
        // from the client. internalEditorNotes starts empty - a client creating a book
        // has no field to populate it from in the first place.
        return new Book(null, request.getTitle(), request.getAuthor(), Instant.now(), "");
    }
}
