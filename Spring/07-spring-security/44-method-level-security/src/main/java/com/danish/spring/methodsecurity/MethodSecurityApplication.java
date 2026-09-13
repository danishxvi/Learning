package com.danish.spring.methodsecurity;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

// ============================================================================
// 44 - METHOD-LEVEL SECURITY
// ============================================================================
// Run: mvn -f Spring/07-spring-security/44-method-level-security spring-boot:run
// ============================================================================
// @EnableMethodSecurity turns on @PreAuthorize/@PostAuthorize/@PreFilter/@PostFilter
// processing at all - without it, every one of those annotations in DocumentService
// would be silently ignored, the same "declared but never wired in" trap as an
// un-registered @Aspect (lesson 39) or un-scanned @Configuration.
@EnableMethodSecurity
@SpringBootApplication
public class MethodSecurityApplication {
    public static void main(String[] args) {
        SpringApplication.run(MethodSecurityApplication.class, args);
    }

    @Component
    static class Demo implements CommandLineRunner {
        private final DocumentService documentService;

        Demo(DocumentService documentService) {
            this.documentService = documentService;
        }

        @Override
        public void run(String... args) {
            System.out.println("=".repeat(74));
            System.out.println("@PreAuthorize(\"hasRole('ADMIN')\") - independent of any HTTP request at all");
            System.out.println("=".repeat(74));
            loginAs("bob", "USER");
            callAndReport("bob (USER) calling deleteAllDocuments()", documentService::deleteAllDocuments);
            loginAs("alice", "ADMIN");
            callAndReport("alice (ADMIN) calling deleteAllDocuments()", documentService::deleteAllDocuments);

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@PreAuthorize(\"#username == authentication.name\") - a parameter-based check");
            System.out.println("=".repeat(74));
            loginAs("bob", "USER");
            callAndReport("bob requesting his OWN profile", () -> documentService.getOwnProfile("bob"));
            callAndReport("bob requesting alice's profile", () -> documentService.getOwnProfile("alice"));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@PostAuthorize(\"returnObject.owner == authentication.name\") - checked on the RESULT");
            System.out.println("=".repeat(74));
            loginAs("bob", "USER");
            callAndReport("bob fetching document #1 (his own)", () -> documentService.getDocumentById(1L));
            callAndReport("bob fetching document #3 (alice's)", () -> documentService.getDocumentById(3L));

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("@PostFilter(\"filterObject.owner == authentication.name\") - filters a returned List");
            System.out.println("=".repeat(74));
            loginAs("bob", "USER");
            System.out.println("  bob sees: " + documentService.getAllDocumentsThenFilter());
            loginAs("alice", "USER");
            System.out.println("  alice sees: " + documentService.getAllDocumentsThenFilter());

            System.out.println();
            System.out.println("=".repeat(74));
            System.out.println("SELF-INVOCATION BYPASSES @PreAuthorize TOO - the same proxy limitation (lesson 38)");
            System.out.println("=".repeat(74));
            loginAs("bob", "USER");
            callAndReport("bob (USER) calling deleteAllDocumentsViaSelfInvocation()",
                    documentService::deleteAllDocumentsViaSelfInvocation);
        }

        private void loginAs(String username, String role) {
            SecurityContextHolder.getContext().setAuthentication(
                    new TestingAuthenticationToken(username, null, "ROLE_" + role));
        }

        private <T> void callAndReport(String label, java.util.function.Supplier<T> call) {
            try {
                System.out.println("  " + label + " -> " + call.get());
            } catch (AccessDeniedException ex) {
                System.out.println("  " + label + " -> AccessDeniedException: " + ex.getMessage());
            }
        }
    }
}
