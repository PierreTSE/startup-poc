package fr.tse.poc.utils;

import fr.tse.poc.authentication.AuthenticableUser;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.*;
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
    @Autowired UserRepository userRepository;
    @Autowired ManagerRepository managerRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired AuthenticableUserRepository authenticableUserRepository;

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
            log.info("Initializing database for test");

            initAdmin();
            initUser();
            initManager();
            initProject();
        };
    }

    private void saveCredentials(People people, Role role) {
        AuthenticableUser authenticableUser = new AuthenticableUser(
                people.getFirstname(),
                bCryptPasswordEncoder.encode("a"),
                role,
                people.getId());
        authenticableUserRepository.save(authenticableUser);
    }

    private void initAdmin() {
        Admin admin = new Admin("admin-firstname", "admin-lastname");
        adminRepository.save(admin);
        saveCredentials(admin, Role.Admin);
    }

    private void initUser() {
        User user1 = new User("user1", "user1-lastname");
        userRepository.save(user1);
        saveCredentials(user1, Role.User);
        User user2 = new User("user2", "user2-lastname");
        userRepository.save(user2);
        saveCredentials(user2, Role.User);
        log.info("Users saved to database.");
    }

    private void initManager() {
        Manager manager1 = new Manager("manager1", "manager1-lastname");
        managerRepository.save(manager1);
        saveCredentials(manager1, Role.Manager);
        Manager manager2 = new Manager("manager2", "manager2-lastname");
        managerRepository.save(manager2);
        saveCredentials(manager2, Role.Manager);
        log.info("Managers saved to database.");
    }

    private void initProject() {
        Project project1 = new Project("project1");
        project1.setManager(managerRepository.findById(1L).orElseThrow());
        User user1 = userRepository.findById(1L).orElseThrow();
        project1.addUser(user1);
        projectRepository.save(project1);
        userRepository.save(user1);
        log.info("Projects saved to database.");
    }
}
