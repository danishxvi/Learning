package com.danish.spring.projections;

// An INTERFACE PROJECTION - no implementation anywhere (same "no implementation, Spring
// Data generates one" pattern as the repository interfaces themselves, lesson 31). Its
// method names (getTitle) must match property names on Book (title). Spring Data uses
// this interface's shape to decide which COLUMNS to select - not all of them, just these.
public interface BookTitleOnly {
    String getTitle();
}
