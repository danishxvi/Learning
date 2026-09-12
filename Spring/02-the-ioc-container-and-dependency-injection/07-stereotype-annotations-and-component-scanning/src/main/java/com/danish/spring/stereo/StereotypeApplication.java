package com.danish.spring.stereo;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.stereotype.Component;

// ============================================================================
// 07 - STEREOTYPE ANNOTATIONS AND COMPONENT SCANNING
// ============================================================================
// Run: mvn -f Spring/02-the-ioc-container-and-dependency-injection/07-stereotype-annotations-and-component-scanning spring-boot:run
//
// This class deliberately writes out what @SpringBootApplication normally hides, so it
// can add ONE extra thing @SpringBootApplication's own @ComponentScan does not expose
// directly: an excludeFilters attribute. Lesson 03 covered what @SpringBootApplication
// expands to; this is that expansion, with one customization added.
// ============================================================================
@SpringBootConfiguration
@EnableAutoConfiguration
@ComponentScan(
        basePackages = "com.danish.spring.stereo",
        excludeFilters = @ComponentScan.Filter(type = FilterType.ANNOTATION, classes = Legacy.class)
)
public class StereotypeApplication {

    public static void main(String[] args) {
        SpringApplication.run(StereotypeApplication.class, args);
    }

    @Component
    static class StereotypeDemo implements CommandLineRunner {

        private final ApplicationContext context;
        private final OrderService orderService;
        private final HomeController homeController;

        StereotypeDemo(ApplicationContext context, OrderService orderService, HomeController homeController) {
            this.context = context;
            this.orderService = orderService;
            this.homeController = homeController;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("@Service AND @Repository, WIRED TOGETHER LIKE ANY OTHER BEAN");
            System.out.println("=".repeat(74));
            orderService.printRecentOrders();

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@Controller, WITHOUT A WEB SERVER, IS JUST A BEAN");
            System.out.println("=".repeat(74));
            System.out.println("  " + homeController.greeting());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("CUSTOM STEREOTYPE: @BusinessLogic FOUND PricingEngine WITHOUT @Component");
            System.out.println("=".repeat(74));
            context.getBeansWithAnnotation(BusinessLogic.class).forEach(
                    (name, bean) -> System.out.println("  " + name + " -> " + bean.getClass().getSimpleName()));
            PricingEngine pricingEngine = context.getBean(PricingEngine.class);
            System.out.println("  Price of a desk-lamp: " + pricingEngine.priceInCents("desk-lamp") + " cents");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("EXCLUDE FILTER: @Legacy KEPT OldPaymentGateway OUT OF THE CONTEXT");
            System.out.println("=".repeat(74));
            String[] excludedBeanNames = context.getBeanNamesForType(OldPaymentGateway.class);
            System.out.println("  Beans of type OldPaymentGateway found: " + excludedBeanNames.length
                    + " (it IS annotated @Component, but @Legacy excluded it from scanning)");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("ALL FOUR CORE STEREOTYPES ARE @Component UNDERNEATH");
            System.out.println("=".repeat(74));
            System.out.println("  @Service on OrderService    -> is @Component? "
                    + OrderService.class.isAnnotationPresent(org.springframework.stereotype.Service.class));
            System.out.println("  @Repository is meta-annotated with @Component: "
                    + org.springframework.stereotype.Repository.class.isAnnotationPresent(Component.class));
            System.out.println("  @Service is meta-annotated with @Component:    "
                    + org.springframework.stereotype.Service.class.isAnnotationPresent(Component.class));
            System.out.println("  @Controller is meta-annotated with @Component: "
                    + org.springframework.stereotype.Controller.class.isAnnotationPresent(Component.class));
        }
    }
}
