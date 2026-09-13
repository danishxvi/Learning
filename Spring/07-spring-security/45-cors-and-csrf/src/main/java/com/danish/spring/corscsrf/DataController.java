package com.danish.spring.corscsrf;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@RestController
public class DataController {

    private final CopyOnWriteArrayList<Map<String, Object>> notes = new CopyOnWriteArrayList<>();
    private final AtomicLong nextId = new AtomicLong(1);

    // A safe, read-only GET - CORS applies to this (a browser on another origin needs
    // permission to READ the response), but CSRF does NOT (CSRF only protects
    // STATE-CHANGING methods - POST/PUT/PATCH/DELETE - a plain GET is assumed safe to
    // trigger cross-site because it isn't supposed to change anything).
    @GetMapping("/api/data")
    public Map<String, Object> data() {
        return Map.of("message", "This is cross-origin data.", "notesCount", notes.size());
    }

    // A STATE-CHANGING POST - this is exactly what CSRF protection exists to guard.
    // No @PreAuthorize, no login required (permitAll in SecurityConfig) - CSRF applies
    // regardless of whether the request is authenticated, as long as the CSRF filter is
    // active at all.
    @PostMapping("/api/notes")
    public Map<String, Object> addNote(@RequestBody Map<String, String> body) {
        long id = nextId.getAndIncrement();
        Map<String, Object> note = Map.of("id", id, "text", body.get("text"));
        notes.add(note);
        return note;
    }
}
