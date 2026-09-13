package com.danish.spring.unittesting;

// A plain interface - not Spring Data JPA here. The real implementation (section 05)
// is irrelevant to THIS lesson: unit testing OrderService means replacing this
// interface with a Mockito MOCK, never touching a real database at all.
public interface OrderRepository {
    Order save(Order order);
}
