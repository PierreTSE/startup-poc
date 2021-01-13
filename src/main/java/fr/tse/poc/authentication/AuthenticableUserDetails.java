package fr.tse.poc.authentication;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthenticableUserDetails implements UserDetails {
    private final AuthenticableUser authenticableUser;

    public AuthenticableUserDetails(AuthenticableUser authenticableUser) {
        this.authenticableUser = authenticableUser;
    }

    @Override public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority(authenticableUser.getRole().toString()));
    }

    @Override public String getPassword() {
        return authenticableUser.getPassword();
    }

    @Override public String getUsername() {
        return authenticableUser.getUsername();
    }

    public Role getRole() {
        return authenticableUser.getRole();
    }

    public Long getForeignId() {
        return authenticableUser.getForeignID();
    }

    @Override public boolean isAccountNonExpired() {
        return true;
    }

    @Override public boolean isAccountNonLocked() {
        return true;
    }

    @Override public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override public boolean isEnabled() {
        return true;
    }
}
