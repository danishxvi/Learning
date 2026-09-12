package com.danish.spring.lifecycle;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

// A BeanPostProcessor runs around EVERY bean's initialization - it is the mechanism
// @PostConstruct itself is built on top of. Registering one lets you hook into, inspect,
// or even REPLACE any bean in the entire context, which is how libraries like AOP proxies
// and @Transactional get wired in without you doing anything (lesson 33, lesson 38).
@Component
public class LoggingBeanPostProcessor implements BeanPostProcessor {

    // Runs BEFORE @PostConstruct / InitializingBean.afterPropertiesSet() for every bean.
    @Override
    public Object postProcessBeforeInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FullLifecycleBean) {
            System.out.println("  [BeanPostProcessor] BEFORE initialization of \"" + beanName + "\" - runs before step 3");
        }
        return bean; // must return the bean (or a replacement) - returning null would remove it
    }

    // Runs AFTER @PostConstruct / InitializingBean.afterPropertiesSet() for every bean.
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (bean instanceof FullLifecycleBean) {
            System.out.println("  [BeanPostProcessor] AFTER initialization of \"" + beanName + "\" - bean is now fully ready");
        }
        return bean;
    }
}
