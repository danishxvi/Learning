package com.danish.spring.webtest;

// A real @Service interface would live here (backed by section 05's Spring Data in a
// real project). This lesson's test NEVER uses a real implementation of it at all -
// @WebMvcTest replaces it with a Mockito mock entirely, see BookControllerTest.
public interface BookService {
    Book findById(Long id);
    Book create(String title, String author);
}
