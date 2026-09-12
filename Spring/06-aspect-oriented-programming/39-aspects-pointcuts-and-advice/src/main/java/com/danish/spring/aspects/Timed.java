package com.danish.spring.aspects;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// A CUSTOM annotation with no behaviour of its own - exactly like lesson 26's @ValidIsbn,
// it is meaningless on its own. TimingAspect below is what gives it meaning, by matching
// an "@annotation(Timed)" pointcut against it.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Timed {
}
