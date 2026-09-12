package com.danish.spring.firstboot;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.stereotype.Component;

// ============================================================================
// 03 - YOUR FIRST SPRING BOOT PROJECT
// ============================================================================
// Run: mvn -f Spring/01-introduction-and-setup/03-your-first-spring-boot-project spring-boot:run
// ============================================================================

// @SpringBootApplication is ONE annotation standing in for three (see the .md for
// exactly which three, and what each one does). It is the difference between this
// class and lesson 02's AppConfig + a manual AnnotationConfigApplicationContext call.
@SpringBootApplication
public class FirstBootApplication {

    public static void main(String[] args) {
        System.out.println("=".repeat(74));
        System.out.println("BEFORE SpringApplication.run() - nothing exists yet");
        System.out.println("=".repeat(74));

        long start = System.nanoTime();

        // Compare this ONE line to lesson 02's:
        //   new AnnotationConfigApplicationContext(AppConfig.class)
        // SpringApplication.run does everything that line did, PLUS: reads
        // application.properties, configures logging, decides (via auto-configuration)
        // which extra beans to register based on what's on the classpath, and returns
        // a running, fully wired ApplicationContext.
        ConfigurableApplicationContext context = SpringApplication.run(FirstBootApplication.class, args);

        long startupMillis = (System.nanoTime() - start) / 1_000_000;

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("AFTER SpringApplication.run() - context is up in " + startupMillis + " ms");
        System.out.println("=".repeat(74));

        // Proves this is the exact same kind of container as lesson 02 - we can still
        // pull beans out of it by hand, even though nothing here asked us to.
        HelloBean helloBean = context.getBean(HelloBean.class);
        System.out.println("Bean pulled from the Boot-managed context: " + helloBean.getClass().getSimpleName());

        context.close();
    }

    // A CommandLineRunner is a bean whose run() method Spring Boot calls automatically,
    // once, right after the context finishes starting - covered fully in lesson 17.
    // For now, it is the easiest way to see "my code" run inside a Boot application.
    @Component
    static class HelloBean implements CommandLineRunner {
        @Override
        public void run(String... args) {
            System.out.println();
            System.out.println(">>> Hello, Spring Boot! This line ran because HelloBean");
            System.out.println(">>> is a @Component that implements CommandLineRunner -");
            System.out.println(">>> component scanning found it with no extra configuration.");
        }
    }
}
