package com.danish.spring.reqmapping;

import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/catalog")
public class CatalogController {

    // ---- PATH VARIABLES -----------------------------------------------------

    // Two path variables, ONE with a name that differs from the method parameter -
    // @PathVariable("category") is required whenever the names don't match, because
    // Spring cannot always recover a parameter's source name from compiled bytecode
    // (it CAN if compiled with -parameters, lesson 15 mentioned this exact flag, but
    // explicit naming here works regardless of that compiler setting).
    @GetMapping("/{category}/{id}")
    public String byCategoryAndId(@PathVariable("category") String categoryName, @PathVariable Long id) {
        return "category=" + categoryName + ", id=" + id + " (id was converted String -> Long automatically)";
    }

    // ---- REQUEST PARAMS -------------------------------------------------------

    // "query" is REQUIRED (no default, no required=false) - omitting it is a 400, not a
    // null value inside the method. "page"/"size" are OPTIONAL, with defaults supplied
    // AS STRINGS - defaultValue is always a String, converted to the target type same
    // as any other request param.
    @GetMapping("/search")
    public String search(@RequestParam String query,
                          @RequestParam(defaultValue = "0") int page,
                          @RequestParam(defaultValue = "10") int size) {
        return "query=\"" + query + "\", page=" + page + ", size=" + size;
    }

    // A request param that can appear MULTIPLE times in the same query string -
    // ?tag=java&tag=spring binds to a List<String> with two elements, in order.
    @GetMapping("/tags")
    public String tags(@RequestParam List<String> tag) {
        return "tags=" + tag;
    }

    // A CATCH-ALL for arbitrary, unknown-in-advance query parameters - binding to a
    // Map<String, String> instead of naming each one individually.
    @GetMapping("/filter")
    public Map<String, String> filter(@RequestParam Map<String, String> allParams) {
        return allParams;
    }

    // ---- REQUEST HEADERS --------------------------------------------------------

    @GetMapping("/whoami")
    public String whoami(@RequestHeader(value = "X-Client-Id", defaultValue = "anonymous") String clientId) {
        return "X-Client-Id header = " + clientId;
    }

    // ---- REQUEST BODY -----------------------------------------------------------

    @PostMapping
    public String create(@RequestBody NewBookRequest request) {
        return "Created: " + request.getTitle() + " by " + request.getAuthor();
    }
}
