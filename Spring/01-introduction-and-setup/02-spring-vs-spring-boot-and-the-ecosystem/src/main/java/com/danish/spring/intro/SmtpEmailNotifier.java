package com.danish.spring.intro;

import org.springframework.stereotype.Component;

// @Component is Spring's version of "register(Notifier.class, c -> new SmtpEmailNotifier())"
// from lesson 01's MiniContainer - except we never call `register` ourselves. At startup,
// Spring's component scanner walks the package tree looking for classes annotated like this
// one, and registers each of them as a "bean" automatically. Covered fully in lesson 07.
@Component
public class SmtpEmailNotifier implements Notifier {

    @Override
    public void send(String message) {
        System.out.println("  [SmtpEmailNotifier] connecting to smtp.example.com ... sent: " + message);
    }
}
