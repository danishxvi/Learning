package com.danish.spring.jwt;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private final UserStore userStore;
    private final PasswordEncoder encoder;
    private final JwtService jwtService;

    public AuthController(UserStore userStore, PasswordEncoder encoder, JwtService jwtService) {
        this.userStore = userStore;
        this.encoder = encoder;
        this.jwtService = jwtService;
    }

    // The ONLY endpoint that checks a password directly. Every OTHER endpoint in this
    // lesson trusts a valid JWT instead - this is the entire point of "stateless": once
    // a token is issued, nothing about this login step needs to happen again for it to
    // keep working, and no server-side session record exists anywhere.
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        if (!userStore.checkPassword(request.getUsername(), request.getPassword(), encoder)) {
            return ResponseEntity.status(401).body(Map.of("error", "Bad credentials"));
        }
        String token = jwtService.generateToken(request.getUsername(), userStore.roleOf(request.getUsername()));
        return ResponseEntity.ok(Map.of("token", token));
    }
}
