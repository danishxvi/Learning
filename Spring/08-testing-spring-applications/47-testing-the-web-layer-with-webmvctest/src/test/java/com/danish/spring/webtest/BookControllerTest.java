package com.danish.spring.webtest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// @WebMvcTest(BookController.class) loads ONLY the web layer for THIS controller -
// @RestController, @RestControllerAdvice, Jackson message converters, Bean Validation -
// and NOTHING else. No @Service, no @Repository, no database, no real BookService
// implementation anywhere on the classpath of this test. This is a narrower slice than
// lesson 46's zero-Spring unit test, but a MUCH narrower slice than a full
// @SpringBootTest (lesson 49) - real Spring MVC request handling, without the rest of
// the application.
@WebMvcTest(BookController.class)
class BookControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // @MockBean replaces the REAL BookService bean in this slice's context with a
    // Mockito mock, registered INTO Spring's context - unlike lesson 46's plain @Mock,
    // this one is actually injected into BookController by the container, the same way
    // a real BookService implementation would be.
    @MockBean
    private BookService bookService;

    @Test
    void returnsABookAsJson() throws Exception {
        when(bookService.findById(1L)).thenReturn(new Book(1L, "Effective Java", "Joshua Bloch"));

        // MockMvc simulates an HTTP request through Spring MVC's REAL dispatching -
        // path matching, argument binding, JSON serialization - WITHOUT a running
        // server or an actual network call.
        mockMvc.perform(get("/api/books/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Effective Java"))
                .andExpect(jsonPath("$.author").value("Joshua Bloch"));
    }

    @Test
    void missingBookIsTranslatedToA404ByTheControllerAdvice() throws Exception {
        when(bookService.findById(999L)).thenThrow(new BookNotFoundException(999L));

        // GlobalExceptionHandler IS part of this slice - proving @RestControllerAdvice
        // actually intercepts the exception thrown by the (mocked) service, exactly as
        // it would with a real one.
        mockMvc.perform(get("/api/books/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("No book with id 999"));
    }

    @Test
    void createsABookAndReturns201() throws Exception {
        when(bookService.create("Clean Code", "Robert C. Martin"))
                .thenReturn(new Book(5L, "Clean Code", "Robert C. Martin"));

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"Clean Code\",\"author\":\"Robert C. Martin\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(5));
    }

    @Test
    void blankTitleFailsValidationBeforeTheServiceIsEverCalled() throws Exception {
        // Bean Validation (lesson 26) runs INSIDE this slice too - @Valid on the
        // controller parameter is real Spring MVC behaviour, not something a plain
        // unit test (lesson 46) could exercise at all.
        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\":\"\",\"author\":\"Robert C. Martin\"}"))
                .andExpect(status().isBadRequest());
    }
}
