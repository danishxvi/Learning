package com.danish.spring.anatomy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;

import java.util.Arrays;

// ============================================================================
// 04 - ANATOMY OF A SPRING BOOT PROJECT
// ============================================================================
// This lesson's point is not this class - it is the FOLDER STRUCTURE and the
// PACKAGED JAR around it. Read the .md alongside this file; it walks the
// project tree and the fat jar this project builds into.
//
// Run: mvn -f Spring/01-introduction-and-setup/04-anatomy-of-a-spring-boot-project spring-boot:run
// ============================================================================
@SpringBootApplication
public class AnatomyApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(AnatomyApplication.class, args);

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("WHAT AUTO-CONFIGURATION REGISTERED, WITHOUT US WRITING A SINGLE @Bean");
        System.out.println("=".repeat(74));

        String[] beanNames = context.getBeanDefinitionNames();
        Arrays.sort(beanNames);
        System.out.println("Total bean definitions in the context: " + beanNames.length);
        System.out.println("This project defines exactly ZERO @Component or @Bean of its own.");
        System.out.println("Every single one of these " + beanNames.length + " beans came from");
        System.out.println("auto-configuration and Spring Boot's own internals (lesson 15 explains how).");
        System.out.println();
        System.out.println("A sample of them:");
        Arrays.stream(beanNames)
                .filter(name -> name.toLowerCase().contains("config") || name.toLowerCase().contains("properties"))
                .limit(8)
                .forEach(name -> System.out.println("  - " + name));

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("WHERE THIS CLASS ACTUALLY LOADED FROM");
        System.out.println("=".repeat(74));
        String location = AnatomyApplication.class.getProtectionDomain().getCodeSource().getLocation().toString();
        System.out.println("Class origin: " + location);
        System.out.println("Under `mvn spring-boot:run` this points at target/classes - your compiled");
        System.out.println(".class files, laid out in the com/danish/spring/anatomy/ folders that mirror");
        System.out.println("the `package com.danish.spring.anatomy;` line at the top of this file.");
        System.out.println("Package `mvn clean package` and run the jar instead, and this same line");
        System.out.println("prints a path INSIDE the fat jar - see the .md for exactly what changes.");

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("WHAT application.properties CONTRIBUTED");
        System.out.println("=".repeat(74));
        Environment env = context.getEnvironment();
        System.out.println("spring.application.name = " + env.getProperty("spring.application.name"));
        System.out.println("Active profiles: " + Arrays.toString(env.getActiveProfiles()) +
                " (empty means the 'default' profile, per lesson 03's startup log line)");

        context.close();
    }
}
