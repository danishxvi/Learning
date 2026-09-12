package com.danish.spring.passwords;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

// ============================================================================
// 42 - PASSWORD ENCODING AND UserDetails
// ============================================================================
// Run: mvn -f Spring/07-spring-security/42-password-encoding-and-userdetails spring-boot:run
// Then, in another terminal:
//   curl -u bob:bob-pass http://localhost:8080/hello   -> 200
//   curl -u eve:eve-pass http://localhost:8080/hello   -> 401 (disabled account, correct password)
// ============================================================================
@SpringBootApplication
public class PasswordsApplication {
    public static void main(String[] args) {
        SpringApplication.run(PasswordsApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final PasswordEncoder encoder;
        private final AuthenticationManager authenticationManager;

        Demo(PasswordEncoder encoder, AuthenticationManager authenticationManager) {
            this.encoder = encoder;
            this.authenticationManager = authenticationManager;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("THE SAME PASSWORD, ENCODED TWICE - two DIFFERENT hashes");
            System.out.println("=".repeat(74));
            String hash1 = encoder.encode("bob-pass");
            String hash2 = encoder.encode("bob-pass");
            System.out.println("  hash1 = " + hash1);
            System.out.println("  hash2 = " + hash2);
            System.out.println("  hash1.equals(hash2)?        " + hash1.equals(hash2));
            System.out.println("  encoder.matches(\"bob-pass\", hash1) = " + encoder.matches("bob-pass", hash1));
            System.out.println("  encoder.matches(\"bob-pass\", hash2) = " + encoder.matches("bob-pass", hash2));
            System.out.println("  BCrypt embeds a random SALT in every hash - that's why they differ - and");
            System.out.println("  matches() re-derives the same salt FROM the stored hash to check correctly.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("WORK FACTOR - a REAL, MEASURED cost difference");
            System.out.println("=".repeat(74));
            timeEncoding("strength 4  (weak, fast)", new BCryptPasswordEncoder(4));
            timeEncoding("strength 10 (Spring's default)", new BCryptPasswordEncoder(10));
            timeEncoding("strength 12 (stronger, slower)", new BCryptPasswordEncoder(12));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("DelegatingPasswordEncoder - the \"{bcrypt}\" PREFIX, and why it exists");
            System.out.println("=".repeat(74));
            PasswordEncoder delegating = PasswordEncoderFactories.createDelegatingPasswordEncoder();
            String delegatingHash = delegating.encode("bob-pass");
            System.out.println("  " + delegatingHash);
            System.out.println("  matches() dispatches to the RIGHT algorithm based on that prefix - so a");
            System.out.println("  system storing some old {noop} or {sha256} hashes and new {bcrypt} ones");
            System.out.println("  can check EITHER kind correctly, without knowing in advance which is which.");

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("AUTHENTICATING FOR REAL - success, wrong password, and a DISABLED account");
            System.out.println("=".repeat(74));
            authenticate("bob", "bob-pass");
            authenticate("bob", "wrong-password");
            authenticate("eve", "eve-pass"); // correct password - account is just disabled
        }

        private void timeEncoding(String label, BCryptPasswordEncoder bcrypt) {
            long start = System.nanoTime();
            bcrypt.encode("bob-pass");
            long elapsedMillis = (System.nanoTime() - start) / 1_000_000;
            System.out.println("  " + label + ": " + elapsedMillis + " ms");
        }

        private void authenticate(String username, String password) {
            try {
                authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(username, password));
                System.out.println("  " + username + "/" + password + " -> SUCCESS");
            } catch (DisabledException ex) {
                System.out.println("  " + username + "/" + password + " -> DisabledException: " + ex.getMessage()
                        + "  (password WAS correct - the account itself is disabled)");
            } catch (BadCredentialsException ex) {
                System.out.println("  " + username + "/" + password + " -> BadCredentialsException: " + ex.getMessage());
            }
        }
    }
}
