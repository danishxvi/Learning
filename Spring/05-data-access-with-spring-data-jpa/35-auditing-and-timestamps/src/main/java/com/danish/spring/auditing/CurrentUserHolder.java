package com.danish.spring.auditing;

// A thread-local stand-in for "who is making this request." A real application sets
// this from an authenticated principal (Spring Security, section 07) - this lesson sets
// it by hand, purely to make @CreatedBy/@LastModifiedBy demonstrable before Security
// exists in this stack.
public class CurrentUserHolder {
    private static final ThreadLocal<String> CURRENT_USER = new ThreadLocal<>();

    public static void set(String username) {
        CURRENT_USER.set(username);
    }

    public static String get() {
        return CURRENT_USER.get();
    }
}
