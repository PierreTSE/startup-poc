package fr.tse.poc.authentication;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthenticableUserRepository extends JpaRepository<AuthenticableUser, String> {
    AuthenticableUser findByUsername(String username);
}
