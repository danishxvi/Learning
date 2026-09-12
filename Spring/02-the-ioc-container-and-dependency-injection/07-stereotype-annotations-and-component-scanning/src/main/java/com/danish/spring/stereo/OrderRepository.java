package com.danish.spring.stereo;

import org.springframework.stereotype.Repository;
import java.util.List;

// @Repository is functionally identical to @Component for scanning purposes - Spring
// registers it as a bean the same way. What makes it different is a SECOND effect:
// Spring registers a PersistenceExceptionTranslationPostProcessor that wraps any bean
// annotated @Repository so that low-level persistence exceptions (JDBC's SQLException,
// JPA's PersistenceException) get translated into Spring's own consistent
// DataAccessException hierarchy - covered fully once JPA is introduced in section 05.
// For now: use @Repository specifically for classes that talk to a database, even
// though nothing here does yet, purely so that translation is already switched on for it.
@Repository
public class OrderRepository {

    public List<String> findRecentOrderIds() {
        return List.of("ORD-1001", "ORD-1002", "ORD-1003");
    }
}
