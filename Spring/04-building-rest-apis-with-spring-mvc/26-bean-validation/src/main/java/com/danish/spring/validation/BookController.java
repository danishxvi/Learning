package com.danish.spring.validation;

import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

// @Valid on the @RequestBody parameter is the ONE thing that turns CreateBookRequest's
// annotations from decoration into an actual check. Without it, every @NotBlank/@Min/
// @ValidIsbn/@Valid(nested) annotation on CreateBookRequest would be silently ignored -
// Spring MVC only runs Bean Validation when explicitly told to, right here.
@RestController
public class BookController {

    @PostMapping("/api/books")
    public ResponseEntity<String> create(@Valid @RequestBody CreateBookRequest request) {
        return ResponseEntity.ok("Created: " + request.getTitle());
    }
}
