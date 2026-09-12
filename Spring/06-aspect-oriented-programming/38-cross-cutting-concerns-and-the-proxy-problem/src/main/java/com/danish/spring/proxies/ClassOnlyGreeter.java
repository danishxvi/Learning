package com.danish.spring.proxies;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// NO interface at all. A JDK dynamic proxy is IMPOSSIBLE here - java.lang.reflect.Proxy
// can only implement interfaces, never subclass a concrete class. Spring's only option
// for a bean shaped like this is a CGLIB proxy: a real runtime SUBCLASS of this exact
// class, the same mechanism lesson 09 used for full-mode @Configuration classes.
@Service
@Transactional
public class ClassOnlyGreeter {
    public String greet(String name) {
        return "Hi, " + name + ".";
    }
}
