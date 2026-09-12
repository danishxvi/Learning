package com.danish.spring.stereo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

// A plain marker annotation - NOT meta-annotated with @Component - used purely as a
// target for an EXCLUDE filter below. Anything carrying @Legacy should be findable by
// grep, but never actually registered as a bean.
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Legacy {
}
