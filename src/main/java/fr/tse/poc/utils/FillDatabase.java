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
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Admin;
import fr.tse.poc.domain.Manager;
import fr.tse.poc.domain.Project;
import fr.tse.poc.domain.User;
import lombok.extern.slf4j.Slf4j;

@Configuration
@Slf4j
public class FillDatabase {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired AdminRepository adminRepository;
    @Autowired UserRepository userRepository;
    @Autowired ManagerRepository managerRepository;
    @Autowired ProjectRepository projectRepository;
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
    
    private void initUser(User user) {
        AuthenticableUser authenticableUser = new AuthenticableUser(
                "user",
                bCryptPasswordEncoder.encode("a"),
                Role.User,
                user.getId());
        authenticableUserRepository.save(authenticableUser);
    }
    
    private void initManager(Manager manager) {
        AuthenticableUser authenticableUser = new AuthenticableUser(
                "manager",
                bCryptPasswordEncoder.encode("a"),
                Role.Manager,
                manager.getId());
        authenticableUserRepository.save(authenticableUser);
    }

    @Bean
    @Profile("test")
    CommandLineRunner initTestDatabase() {
        return args -> {
            log.info("Initializing database for test");

            initAdmin();

            User user1 = new User("Jean", "Bon");
            Manager manager1 = new Manager("Jeremy","Monslip");
            
            managerRepository.save(manager1);
            log.info(manager1.getFullName() + " saved to database.");
            
            user1.setManager(managerRepository.getOne(manager1.getId()));
            
            userRepository.save(user1);
            log.info(user1.getFullName() + " saved to database.");
            
            initUser(user1);
            initManager(manager1);  
            log.info("Manager and user logins saved to database.");

            
            User user2 = new User("Sarah", "Fraichit");
            userRepository.save(user2);
            log.info(user2.getFullName() + " saved to database.");
            
            Project projet1=new Project();
            projet1.setName("Death Star");
            projet1.setManager(manager1);
            projet1.getUsers().add(user1);
            projectRepository.save(projet1);
            log.info(projet1.getName() + " saved to database.");

        };
    }
}
