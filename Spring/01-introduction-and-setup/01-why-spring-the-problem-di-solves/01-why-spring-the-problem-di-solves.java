import java.util.HashMap;
import java.util.Map;

// ============================================================================
// 01 - WHY SPRING - THE PROBLEM DEPENDENCY INJECTION SOLVES
// ============================================================================
// This file has NOTHING to do with the Spring framework. That is the point.
// Before you can appreciate what Spring's container does, you have to feel
// the pain of doing its job by hand. Run this file top to bottom; each demo
// builds on the previous one's fix.
//
// Run: java Spring/01-introduction-and-setup/01-why-spring-the-problem-di-solves/01-why-spring-the-problem-di-solves.java
// ============================================================================

class WhySpringTheProblemDiSolves {

    public static void main(String[] args) {
        System.out.println("=".repeat(74));
        System.out.println("DEMO 1: TIGHT COUPLING - THE PROBLEM");
        System.out.println("=".repeat(74));
        demoTightCoupling();

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("DEMO 2: PROGRAMMING TO AN INTERFACE - HALF A FIX");
        System.out.println("=".repeat(74));
        demoInterfaceStillCoupled();

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("DEMO 3: DEPENDENCY INJECTION BY HAND");
        System.out.println("=".repeat(74));
        demoManualDependencyInjection();

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("DEMO 4: THE WIRING PROBLEM AT SCALE");
        System.out.println("=".repeat(74));
        demoWiringExplosion();

        System.out.println();
        System.out.println("=".repeat(74));
        System.out.println("DEMO 5: A HAND-ROLLED MINI CONTAINER");
        System.out.println("=".repeat(74));
        demoMiniContainer();
    }

    // ------------------------------------------------------------------
    // DEMO 1 - a class that CREATES its own dependency with `new`.
    // ------------------------------------------------------------------
    static void demoTightCoupling() {
        OrderServiceTightlyCoupled service = new OrderServiceTightlyCoupled();
        service.placeOrder("laptop");

        System.out.println();
        System.out.println("Look inside OrderServiceTightlyCoupled: it has a hardcoded");
        System.out.println("`new SmtpEmailSender()` in its own constructor. Consequences:");
        System.out.println("  1. You cannot test it without ALSO connecting to a real SMTP server.");
        System.out.println("  2. You cannot swap in an SmsNotifier without editing this class.");
        System.out.println("  3. Every class that needs notifications repeats this same `new`.");
    }

    /** A notifier that "sends" an email. In real code this would open a socket. */
    static class SmtpEmailSender {
        void send(String message) {
            System.out.println("  [SmtpEmailSender] connecting to smtp.example.com ... sent: " + message);
        }
    }

    /** BAD: this class decides for itself, at compile time, exactly how it will send notifications. */
    static class OrderServiceTightlyCoupled {
        // The dependency is created HERE, inside the class that uses it.
        // OrderService and SmtpEmailSender are now welded together.
        private final SmtpEmailSender emailSender = new SmtpEmailSender();

        void placeOrder(String product) {
            System.out.println("  Order placed for: " + product);
            emailSender.send("Your order for " + product + " has shipped.");
        }
    }

    // ------------------------------------------------------------------
    // DEMO 2 - programming to an interface fixes the TYPE coupling but not
    // the CREATION coupling. `new` is still inside the class that uses it.
    // ------------------------------------------------------------------
    static void demoInterfaceStillCoupled() {
        OrderServiceStillCoupled service = new OrderServiceStillCoupled();
        service.placeOrder("keyboard");

        System.out.println();
        System.out.println("Better: OrderService now depends on the Notifier INTERFACE, not a");
        System.out.println("concrete SmtpEmailSender class. But look at the constructor - it still");
        System.out.println("does `new SmtpEmailSender()` itself. The interface helped nothing yet,");
        System.out.println("because OrderService still decides, and still creates, its own dependency.");
    }

    interface Notifier {
        void send(String message);
    }

    static class SmtpEmailNotifier implements Notifier {
        public void send(String message) {
            System.out.println("  [SmtpEmailNotifier] sent: " + message);
        }
    }

    static class SmsNotifier implements Notifier {
        public void send(String message) {
            System.out.println("  [SmsNotifier] texted: " + message);
        }
    }

    /** STILL BAD: depends on an interface, but still constructs its own implementation. */
    static class OrderServiceStillCoupled {
        private final Notifier notifier = new SmtpEmailNotifier(); // still hardcoded

        void placeOrder(String product) {
            System.out.println("  Order placed for: " + product);
            notifier.send("Your order for " + product + " has shipped.");
        }
    }

    // ------------------------------------------------------------------
    // DEMO 3 - Dependency Injection: the dependency is handed IN from
    // outside, through the constructor. OrderService no longer knows or
    // cares which Notifier implementation it got.
    // ------------------------------------------------------------------
    static void demoManualDependencyInjection() {
        // The CALLER decides which implementation to use, and passes it in.
        OrderService emailBackedService = new OrderService(new SmtpEmailNotifier());
        emailBackedService.placeOrder("monitor");

        OrderService smsBackedService = new OrderService(new SmsNotifier());
        smsBackedService.placeOrder("mouse");

        // Testing needs no real network call at all - just a fake implementation.
        OrderService testService = new OrderService(message ->
                System.out.println("  [FakeNotifier for tests] captured: " + message));
        testService.placeOrder("test-widget");

        System.out.println();
        System.out.println("This is Dependency Injection, with no framework involved:");
        System.out.println("  - OrderService declares what it NEEDS (a Notifier) in its constructor.");
        System.out.println("  - OrderService does not know or care WHICH Notifier it gets.");
        System.out.println("  - The decision moves to whoever calls `new OrderService(...)`.");
        System.out.println("  - Testing needs no real SMTP server - just pass a fake Notifier.");
        System.out.println("This one change is the entire idea. Everything Spring does with");
        System.out.println("@Autowired and @Component is automation built on top of exactly this.");
    }

    /** GOOD: the dependency is a constructor PARAMETER, not something this class creates. */
    static class OrderService {
        private final Notifier notifier;

        // This is called "constructor injection." The Notifier is INJECTED
        // (handed in) rather than the class reaching out and creating one.
        OrderService(Notifier notifier) {
            this.notifier = notifier;
        }

        void placeOrder(String product) {
            System.out.println("  Order placed for: " + product);
            notifier.send("Your order for " + product + " has shipped.");
        }
    }

    // ------------------------------------------------------------------
    // DEMO 4 - DI solves coupling but creates a new chore: SOMEONE has to
    // build the whole object graph, in the right order, by hand. In a
    // real application with dozens of services, this "wiring" code grows
    // into its own maintenance burden.
    // ------------------------------------------------------------------
    static void demoWiringExplosion() {
        // Building this small graph by hand already takes six lines, in a
        // very particular order - PaymentGateway before PaymentService,
        // both before Checkout. Get the order wrong and it will not compile,
        // or worse, compile but wire the wrong instance in.
        Notifier notifier = new SmtpEmailNotifier();
        InventoryChecker inventoryChecker = new InventoryChecker();
        PaymentGateway paymentGateway = new StripePaymentGateway();
        PaymentService paymentService = new PaymentService(paymentGateway, notifier);
        Checkout checkout = new Checkout(paymentService, inventoryChecker, notifier);

        checkout.buy("headphones");

        System.out.println();
        System.out.println("Six `new` calls, in a specific dependency order, just to build ONE");
        System.out.println("Checkout object. A real application has hundreds of classes like this.");
        System.out.println("Someone (or something) still has to:");
        System.out.println("  1. Know every class that exists.");
        System.out.println("  2. Know the order to construct them in.");
        System.out.println("  3. Re-wire everything whenever a constructor's parameters change.");
        System.out.println("This manual assembly code is usually called 'the wiring', and it is");
        System.out.println("exactly the job Spring's IoC container takes over in lesson 06.");
    }

    interface PaymentGateway {
        boolean charge(int amountCents);
    }

    static class StripePaymentGateway implements PaymentGateway {
        public boolean charge(int amountCents) {
            System.out.println("  [StripePaymentGateway] charged " + amountCents + " cents");
            return true;
        }
    }

    static class InventoryChecker {
        boolean isInStock(String product) {
            return true; // pretend everything is in stock
        }
    }

    static class PaymentService {
        private final PaymentGateway gateway;
        private final Notifier notifier;

        PaymentService(PaymentGateway gateway, Notifier notifier) {
            this.gateway = gateway;
            this.notifier = notifier;
        }

        boolean pay(String product, int amountCents) {
            boolean charged = gateway.charge(amountCents);
            if (charged) {
                notifier.send("Payment of " + amountCents + " cents received for " + product);
            }
            return charged;
        }
    }

    static class Checkout {
        private final PaymentService paymentService;
        private final InventoryChecker inventoryChecker;
        private final Notifier notifier;

        Checkout(PaymentService paymentService, InventoryChecker inventoryChecker, Notifier notifier) {
            this.paymentService = paymentService;
            this.inventoryChecker = inventoryChecker;
            this.notifier = notifier;
        }

        void buy(String product) {
            if (!inventoryChecker.isInStock(product)) {
                System.out.println("  Out of stock: " + product);
                return;
            }
            boolean paid = paymentService.pay(product, 5_000);
            if (paid) {
                notifier.send("Your order for " + product + " is confirmed.");
            }
        }
    }

    // ------------------------------------------------------------------
    // DEMO 5 - a tiny, hand-rolled version of what an IoC container does:
    // a registry of "how to build each type", built once, that resolves
    // constructor parameters automatically by matching types. This is a
    // toy - real Spring uses reflection, not a switch statement - but the
    // SHAPE of the idea is identical, and seeing it built removes the
    // magic before lesson 06 introduces the real ApplicationContext.
    // ------------------------------------------------------------------
    static void demoMiniContainer() {
        MiniContainer container = new MiniContainer();

        // Register how to build each object. Order does not matter here -
        // the container resolves dependencies lazily, on first request.
        container.register(Notifier.class, c -> new SmtpEmailNotifier());
        container.register(InventoryChecker.class, c -> new InventoryChecker());
        container.register(PaymentGateway.class, c -> new StripePaymentGateway());
        container.register(PaymentService.class,
                c -> new PaymentService(c.get(PaymentGateway.class), c.get(Notifier.class)));
        container.register(Checkout.class,
                c -> new Checkout(c.get(PaymentService.class), c.get(InventoryChecker.class), c.get(Notifier.class)));

        // The caller no longer wires anything by hand - it just ASKS the
        // container for a fully-built Checkout, in any order it likes.
        Checkout checkout = container.get(Checkout.class);
        checkout.buy("desk-lamp");

        // The container also caches: asking twice returns the SAME instance.
        Checkout again = container.get(Checkout.class);
        System.out.println();
        System.out.println("Same instance returned on second request: " + (checkout == again));

        System.out.println();
        System.out.println("This MiniContainer is roughly forty lines long and already does the");
        System.out.println("core job: given a type, build (or reuse) an instance, resolving its");
        System.out.println("constructor dependencies recursively. Spring's ApplicationContext is");
        System.out.println("this same idea, built out with annotations, scopes, lifecycle callbacks,");
        System.out.println("and auto-configuration - covered starting in section 02.");
    }

    /** A factory function that knows how to build ONE type, given a container to pull its own dependencies from. */
    interface Factory<T> {
        T create(MiniContainer container);
    }

    /**
     * A minimal, hand-rolled stand-in for an IoC container: a registry of
     * factories, plus a cache of already-built singletons. This is what
     * Spring's {@code ApplicationContext} does for you automatically.
     */
    static class MiniContainer {
        private final Map<Class<?>, Factory<?>> factories = new HashMap<>();
        private final Map<Class<?>, Object> singletons = new HashMap<>();

        <T> void register(Class<T> type, Factory<T> factory) {
            factories.put(type, factory);
        }

        @SuppressWarnings("unchecked")
        <T> T get(Class<T> type) {
            if (singletons.containsKey(type)) {
                return (T) singletons.get(type);
            }
            Factory<T> factory = (Factory<T>) factories.get(type);
            if (factory == null) {
                throw new IllegalStateException("No factory registered for " + type.getSimpleName());
            }
            T instance = factory.create(this); // recursively resolves this type's own dependencies
            singletons.put(type, instance);
            return instance;
        }
    }
}
