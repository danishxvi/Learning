package com.danish.spring.paging;

import java.util.List;

// A generic "page envelope" - the shape almost every paginated API returns, regardless
// of what's paginated. Section 05 introduces Spring Data's own Page<T> interface, which
// carries exactly this same information once a real database is involved - this record
// is the plain, from-scratch version of that same idea.
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static <T> PageResponse<T> of(List<T> pageContent, int page, int size, long totalElements) {
        int totalPages = (int) Math.ceil((double) totalElements / size);
        return new PageResponse<>(pageContent, page, size, totalElements, totalPages);
    }
}
