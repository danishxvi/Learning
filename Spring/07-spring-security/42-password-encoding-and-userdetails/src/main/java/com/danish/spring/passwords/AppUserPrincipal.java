package com.danish.spring.passwords;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;

// The ADAPTER between your own domain object and what Spring Security actually needs.
// Keeping AppUserAccount free of this interface means your persistence/domain layer
// never depends on a security-framework type - if the framework changed, only this
// class would need to.
public class AppUserPrincipal implements UserDetails {
    private final AppUserAccount account;

    public AppUserPrincipal(AppUserAccount account) {
        this.account = account;
    }

    @Override
    public String getUsername() { return account.getUsername(); }

    @Override
    public String getPassword() { return account.getPasswordHash(); }

    @Override
    public List<GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_" + account.getRole()));
    }

    // These four flags are the OTHER half of "authentication" beyond a correct
    // password - a correct password on a DISABLED account should still fail, and this
    // is exactly the hook Spring Security calls to check that, automatically, on every
    // authentication attempt.
    @Override
    public boolean isEnabled() { return account.isEnabled(); }

    @Override
    public boolean isAccountNonExpired() { return true; }

    @Override
    public boolean isAccountNonLocked() { return true; }

    @Override
    public boolean isCredentialsNonExpired() { return true; }
}
