package com.danish.spring.configbeans;

import com.danish.spring.external.FullProxyConfig;
import com.danish.spring.external.LiteConfig;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.stereotype.Component;

// ============================================================================
// 09 - @Configuration CLASSES AND @Bean METHODS
// ============================================================================
// Run: mvn -f Spring/02-the-ioc-container-and-dependency-injection/09-configuration-classes-and-bean-methods spring-boot:run
// ============================================================================
@SpringBootApplication
public class ConfigBeansApplication {

    public static void main(String[] args) {
        SpringApplication.run(ConfigBeansApplication.class, args);
    }

    @Component
    static class ConfigBeansDemo implements CommandLineRunner {

        private final Car car;
        private final Engine engineBean;
        private final ThirdPartyConnectionPool connectionPool;

        ConfigBeansDemo(Car car, Engine engineBean, ThirdPartyConnectionPool connectionPool) {
            this.car = car;
            this.engineBean = engineBean;
            this.connectionPool = connectionPool;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("REGISTERING A THIRD-PARTY CLASS VIA @Bean (ThirdPartyConfig)");
            System.out.println("=".repeat(74));
            System.out.println("  car.getEngine()          = " + car.getEngine());
            System.out.println("  context's Engine bean    = " + engineBean);
            System.out.println("  Same instance? " + (car.getEngine() == engineBean));
            System.out.println("  (connectionPool.connect() already ran automatically via initMethod - see the log above)");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("FULL MODE (proxyBeanMethods = true, the default)");
            System.out.println("=".repeat(74));
            try (AnnotationConfigApplicationContext fullContext =
                         new AnnotationConfigApplicationContext(FullProxyConfig.class)) {
                Engine contextEngine = fullContext.getBean(Engine.class);
                Engine carsEngine = fullContext.getBean(Car.class).getEngine();
                System.out.println("  engine() ran ONCE despite two references to it (context bean + car's copy).");
                System.out.println("  contextEngine == carsEngine ? " + (contextEngine == carsEngine));
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("LITE MODE (proxyBeanMethods = false)");
            System.out.println("=".repeat(74));
            try (AnnotationConfigApplicationContext liteContext =
                         new AnnotationConfigApplicationContext(LiteConfig.class)) {
                Engine contextEngine = liteContext.getBean(Engine.class);
                Engine carsEngine = liteContext.getBean(Car.class).getEngine();
                System.out.println("  engine() ran TWICE - once for the context bean, once inside car().");
                System.out.println("  contextEngine == carsEngine ? " + (contextEngine == carsEngine)
                        + "  <-- car's Engine is NOT the one registered in the context!");
            }

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("WHY LITE MODE EXISTS ANYWAY");
            System.out.println("=".repeat(74));
            System.out.println("  CGLIB proxying costs a little startup time and requires a non-final class");
            System.out.println("  with a no-arg constructor. Lite mode skips all of that - the right choice");
            System.out.println("  whenever a @Configuration class's @Bean methods never call each other.");
        }
    }
}
