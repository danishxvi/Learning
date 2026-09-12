package com.danish.spring.lifecycle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

// ============================================================================
// 06 - THE ApplicationContext AND THE BEAN LIFECYCLE
// ============================================================================
// Run: mvn -f Spring/02-the-ioc-container-and-dependency-injection/06-the-applicationcontext-and-the-bean-lifecycle spring-boot:run
// ============================================================================
@SpringBootApplication
public class LifecycleApplication {

    public static void main(String[] args) {
        System.out.println("=".repeat(74));
        System.out.println("STARTUP - watch the numbered steps appear in order");
        System.out.println("=".repeat(74));

        ConfigurableApplicationContext context = SpringApplication.run(LifecycleApplication.class, args);

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("CONTEXT IS FULLY UP - the bean has been usable for a while now");
        System.out.println("=".repeat(74));
        context.getBean(FullLifecycleBean.class).doWork();

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("SHUTDOWN - calling context.close() triggers the destroy hooks");
        System.out.println("=".repeat(74));
        context.close();

        System.out.println();
        System.out.println("Notice #3 and #4 both ran (in that order) around the BeanPostProcessor's");
        System.out.println("BEFORE/AFTER lines, and #5/#6 both ran on close(). A real bean should pick");
        System.out.println("ONE initialization mechanism and ONE destruction mechanism - never both -");
        System.out.println("this class used every one just to make the ordering visible.");
    }
}
