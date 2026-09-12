package com.danish.spring.auditing;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NoteService {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public Long create(String content) {
        Note note = new Note(content);
        entityManager.persist(note);
        return note.getId();
    }

    // Dirty checking (lesson 29) triggers the UPDATE; the SAME AuditingEntityListener
    // that filled createdDate/createdBy on insert fills lastModifiedDate/lastModifiedBy
    // again here, via JPA's @PreUpdate callback - no code in this method mentions
    // auditing at all.
    @Transactional
    public void updateContent(Long id, String newContent) {
        Note note = entityManager.find(Note.class, id);
        note.setContent(newContent);
    }
}
