package com.danish.spring.configbeans;

// Another stand-in for a class you cannot annotate - it has its own connect()/shutdown()
// methods with library-specific names, not Spring's init/destroy conventions. @Bean's
// initMethod/destroyMethod attributes (see ThirdPartyConfig) bridge that gap without
// wrapping this class in anything.
public class ThirdPartyConnectionPool {

    public void connect() {
        System.out.println("  [ThirdPartyConnectionPool] connect() called - pretend this opens 10 connections");
    }

    public void shutdown() {
        System.out.println("  [ThirdPartyConnectionPool] shutdown() called - pretend this closes them all");
    }
}
