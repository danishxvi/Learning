package com.danish.spring.proxies;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

// Before looking at what SPRING does, build the exact same mechanism by hand, with
// nothing but the JDK - this is the "MiniContainer" of lesson 01, but for AOP.
public class HandBuiltProxyDemo {

    public interface Notifier {
        void send(String message);
    }

    static class RealNotifier implements Notifier {
        @Override
        public void send(String message) {
            System.out.println("    [RealNotifier] sending: " + message);
        }
    }

    // An InvocationHandler is called for EVERY method invoked on the proxy - it decides
    // what actually happens. This one runs code before and after, then delegates to the
    // real object - the exact shape of "cross-cutting" behaviour: logging wrapped around
    // business logic, without one line of logging code inside RealNotifier itself.
    static class LoggingInvocationHandler implements InvocationHandler {
        private final Object target;

        LoggingInvocationHandler(Object target) {
            this.target = target;
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("    [proxy] BEFORE " + method.getName());
            Object result = method.invoke(target, args);
            System.out.println("    [proxy] AFTER " + method.getName());
            return result;
        }
    }

    public static void run() {
        RealNotifier real = new RealNotifier();

        // Proxy.newProxyInstance builds a class IMPLEMENTING Notifier, at runtime, whose
        // every method forwards to the InvocationHandler above - not to RealNotifier
        // directly. This is the JDK dynamic proxy mechanism itself - lesson 31's
        // jdk.proxy2.$Proxy94 was built by this exact API, just called by Spring instead
        // of by hand.
        Notifier proxy = (Notifier) Proxy.newProxyInstance(
                HandBuiltProxyDemo.class.getClassLoader(),
                new Class<?>[]{Notifier.class},
                new LoggingInvocationHandler(real));

        System.out.println("  proxy.getClass()          = " + proxy.getClass());
        System.out.println("  Notifier.class.isInstance = " + Notifier.class.isInstance(proxy));
        System.out.println("  calling proxy.send(...):");
        proxy.send("Hello from a hand-built proxy");
    }
}
