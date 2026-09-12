package com.danish.spring.lifecycle;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.BeanNameAware;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

// This bean implements every lifecycle hook Spring offers, on purpose, so we can print
// the ACTUAL order they run in rather than trust a diagram. Real beans should pick ONE
// mechanism (see the .md for which); this one uses all of them only to prove the order.
@Component
public class FullLifecycleBean implements BeanNameAware, InitializingBean, DisposableBean {

    // 1. CONSTRUCTOR - dependencies are already injected by the time this runs.
    public FullLifecycleBean() {
        System.out.println("  1. Constructor ran - object exists, but is not yet a fully-configured bean");
    }

    // 2. AWARE INTERFACES - the container tells the bean things about itself.
    // BeanNameAware is one of several *Aware interfaces (others: ApplicationContextAware,
    // EnvironmentAware) that let a bean ask the container for container-level information.
    @Override
    public void setBeanName(String name) {
        System.out.println("  2. BeanNameAware.setBeanName() - this bean's registered name is \"" + name + "\"");
    }

    // 3. @PostConstruct - runs after dependency injection, before the bean is handed out.
    // This is the RECOMMENDED way to run initialization logic - it needs no Spring-specific
    // interface, just a standard jakarta.annotation.
    @PostConstruct
    public void postConstruct() {
        System.out.println("  3. @PostConstruct - dependencies are set, safe to do setup work here");
    }

    // 4. InitializingBean.afterPropertiesSet() - the older, interface-based equivalent of
    // @PostConstruct. Runs immediately after it. Coupling a bean to a Spring interface is
    // why @PostConstruct is generally preferred over implementing InitializingBean.
    @Override
    public void afterPropertiesSet() {
        System.out.println("  4. InitializingBean.afterPropertiesSet() - the interface-based equivalent of #3");
    }

    // Business logic - what this bean actually exists to do, once fully initialized.
    public void doWork() {
        System.out.println("  ... doWork() called while the application is running ...");
    }

    // 5. @PreDestroy - runs on container shutdown, before the bean is discarded. The
    // RECOMMENDED way to release resources (close connections, flush buffers).
    @PreDestroy
    public void preDestroy() {
        System.out.println("  5. @PreDestroy - container is shutting down, release resources here");
    }

    // 6. DisposableBean.destroy() - the interface-based equivalent of @PreDestroy, and for
    // the same reason as InitializingBean, usually skipped in favour of the annotation.
    @Override
    public void destroy() {
        System.out.println("  6. DisposableBean.destroy() - the interface-based equivalent of #5");
    }
}
