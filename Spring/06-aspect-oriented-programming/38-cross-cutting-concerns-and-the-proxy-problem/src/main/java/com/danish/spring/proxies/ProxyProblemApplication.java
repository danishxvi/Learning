package com.danish.spring.proxies;

import org.springframework.aop.support.AopUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;

// ============================================================================
// 38 - CROSS-CUTTING CONCERNS AND THE PROXY PROBLEM
// ============================================================================
// Default run (Spring Boot's own default - CGLIB even for interface-backed beans):
//   mvn -f Spring/06-aspect-oriented-programming/38-cross-cutting-concerns-and-the-proxy-problem spring-boot:run
// Forcing JDK dynamic proxies where possible:
//   mvn -f Spring/06-aspect-oriented-programming/38-cross-cutting-concerns-and-the-proxy-problem spring-boot:run -Dspring-boot.run.arguments=--spring.aop.proxy-target-class=false
// ============================================================================
@SpringBootApplication
public class ProxyProblemApplication {
    public static void main(String[] args) {
        SpringApplication.run(ProxyProblemApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final GreetingService interfaceBacked;
        private final ClassOnlyGreeter classOnly;

        Demo(GreetingService interfaceBacked, ClassOnlyGreeter classOnly) {
            this.interfaceBacked = interfaceBacked;
            this.classOnly = classOnly;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("A HAND-BUILT JDK DYNAMIC PROXY - no Spring involved at all");
            System.out.println("=".repeat(74));
            HandBuiltProxyDemo.run();

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("WHAT KIND OF PROXY DID SPRING ACTUALLY BUILD?");
            System.out.println("=".repeat(74));
            System.out.println("  GreetingService (implements an interface):");
            System.out.println("    getClass()        = " + interfaceBacked.getClass());
            System.out.println("    isCglibProxy      = " + AopUtils.isCglibProxy(interfaceBacked));
            System.out.println("    isJdkDynamicProxy = " + AopUtils.isJdkDynamicProxy(interfaceBacked));

            System.out.println();
            System.out.println("  ClassOnlyGreeter (NO interface):");
            System.out.println("    getClass()        = " + classOnly.getClass());
            System.out.println("    isCglibProxy      = " + AopUtils.isCglibProxy(classOnly));
            System.out.println("    isJdkDynamicProxy = " + AopUtils.isJdkDynamicProxy(classOnly));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("THE SELF-INVOCATION PROBLEM, FROM THE PROXY'S OWN POINT OF VIEW");
            System.out.println("=".repeat(74));
            System.out.println("  Calling greetTwice(), which internally calls greet() via `this.`:");
            System.out.println("    " + interfaceBacked.greetTwice("Danish"));
            System.out.println("  Both calls to greet() still ran correctly - self-invocation only breaks");
            System.out.println("  ADVICE that specifically targets greet() (like lesson 33's REQUIRES_NEW),");
            System.out.println("  not the plain method call itself. See the .md for exactly why.");
        }
    }
}
