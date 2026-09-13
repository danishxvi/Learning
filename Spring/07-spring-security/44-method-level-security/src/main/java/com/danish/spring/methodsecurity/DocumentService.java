package com.danish.spring.methodsecurity;

import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PostFilter;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DocumentService {

    private final List<Document> allDocuments = List.of(
            new Document(1L, "bob", "Bob's first document"),
            new Document(2L, "bob", "Bob's second document"),
            new Document(3L, "alice", "Alice's document")
    );

    // @PreAuthorize runs BEFORE the method body - the SpEL expression is checked first,
    // and the method never executes at all if it evaluates to false. This has NOTHING
    // to do with any HTTP request or URL pattern (lessons 40-41's authorizeHttpRequests)
    // - it applies exactly as well to a plain @Service method called from anywhere,
    // including a background job or a scheduled task with no web request in sight.
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteAllDocuments() {
        return "All documents deleted (pretend).";
    }

    // SpEL in @PreAuthorize can reference the method's OWN PARAMETERS by name (#username)
    // and the current Authentication (via the built-in "authentication" variable) - this
    // expression implements "you may only look up your own profile," entirely declaratively.
    @PreAuthorize("#username == authentication.name")
    public String getOwnProfile(String username) {
        return "Profile data for " + username;
    }

    // @PostAuthorize runs AFTER the method body, checking the RETURN VALUE via
    // "returnObject" - necessary here because whether bob is allowed to see THIS
    // document depends on data (the document's owner) that doesn't exist until the
    // method has already fetched it. @PreAuthorize could not express this check at all.
    @PostAuthorize("returnObject.owner == authentication.name")
    public Document getDocumentById(Long id) {
        return allDocuments.stream().filter(d -> d.getId().equals(id)).findFirst().orElseThrow();
    }

    // @PostFilter removes elements from the RETURNED COLLECTION that don't satisfy the
    // expression - "filterObject" refers to each individual element in turn. Critically,
    // it does this by calling remove()/clear() on the collection ITSELF, in place - it
    // does NOT build a new filtered list. Returning the shared immutable allDocuments
    // list here would throw UnsupportedOperationException the moment @PostFilter tried
    // to remove anything from it - a fresh, mutable ArrayList is required every call.
    @PostFilter("filterObject.owner == authentication.name")
    public List<Document> getAllDocumentsThenFilter() {
        return new java.util.ArrayList<>(allDocuments);
    }

    // Calls deleteAllDocuments() via `this.` - PLAIN Java, bypassing whatever proxy
    // wraps this bean, exactly like lesson 38's self-invocation demo. @PreAuthorize is
    // enforced by that SAME proxy mechanism, so it is bypassed here just as
    // completely as lesson 33's @Transactional(REQUIRES_NEW) was.
    public String deleteAllDocumentsViaSelfInvocation() {
        return this.deleteAllDocuments();
    }
}
