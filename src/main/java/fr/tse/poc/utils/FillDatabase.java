package fr.tse.poc.utils;

import fr.tse.poc.authentication.AuthenticableUser;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.domain.Admin;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

@Configuration
@Slf4j
public class FillDatabase {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired AdminRepository adminRepository;
    @Autowired AuthenticableUserRepository authenticableUserRepository;

    private void initAdmin() {
        Admin admin = new Admin("admin-firstname", "admin-lastname");
        AuthenticableUser authenticableUser = new AuthenticableUser(
                "a",
                bCryptPasswordEncoder.encode("a"),
                Role.Admin,
                adminRepository.save(admin).getId());
        authenticableUserRepository.save(authenticableUser);
    }

    @Bean
    @Profile("!test")
    CommandLineRunner initDatabase() {
        return args -> {
            initAdmin();
        };
    }

    @Bean
    @Profile("test")
    CommandLineRunner initTestDatabase() {
        return args -> {

        };
    }
}
