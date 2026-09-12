package com.danish.spring.stereo;

import org.springframework.stereotype.Component;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// A CUSTOM stereotype annotation. @Component itself is meta-annotated onto @Service,
// @Repository and @Controller (they are not special-cased in the scanner - the scanner
// just looks for anything ITSELF annotated, directly or transitively, with @Component).
// Meta-annotating @Component here means @BusinessLogic gets picked up by component
// scanning exactly like @Service would - this is how a codebase (or a company-wide
// internal library) invents its own named stereotypes.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Component
public @interface BusinessLogic {
}
