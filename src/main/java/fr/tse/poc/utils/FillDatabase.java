package fr.tse.poc.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import fr.tse.poc.authentication.AuthenticableUser;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Admin;
import fr.tse.poc.domain.User;
import lombok.extern.slf4j.Slf4j;

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
    CommandLineRunner initTestDatabase(UserRepository userRepository) {
        return args -> {
        	Admin admin = new Admin("admin-firstname", "admin-lastname");
            AuthenticableUser authenticableUser = new AuthenticableUser(
                    "a",
                    bCryptPasswordEncoder.encode("a"),
                    Role.Admin,
                    adminRepository.save(admin).getId());
            authenticableUserRepository.save(authenticableUser);
        	
        	
        	User user1=new User("Jean","Bon");
        	userRepository.save(user1);
			log.info(user1 + " saved to database.");
			
			log.info("Wow it seems OK for Kanban app initialization !");
        };
    }
}
