package com.danish.spring.repositories;

import org.springframework.data.jpa.repository.JpaRepository;

// This interface has NO implementation anywhere in this project - no class implements
// it. Just extending JpaRepository<Book, Long> (entity type, id type) is enough for
// Spring Data to generate save(), findById(), findAll(), deleteById(), count(),
// existsById(), and more, entirely at startup. Nothing here is hand-written.
//
// JpaRepository extends PagingAndSortingRepository (adds Pageable/Sort support -
// lesson 34), which extends CrudRepository (the basic save/find/delete operations) -
// each layer adding capability on top of the one below it.
public interface BookRepository extends JpaRepository<Book, Long> {
}
