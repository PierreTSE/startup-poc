package fr.tse.poc.utils;

import fr.tse.poc.authentication.AuthenticableUser;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.*;
import fr.tse.poc.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Configuration
@Slf4j
public class FillDatabase {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired AdminRepository adminRepository;
    @Autowired UserRepository userRepository;
    @Autowired ManagerRepository managerRepository;
    @Autowired ProjectRepository projectRepository;
    @Autowired TimeCheckRepository timeCheckRepository;
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
            initManager();
            initUser();
            initProject();
            initTimeChecks();
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

    private void initManager() {
        Manager manager1 = new Manager("manager1", "manager1-lastname");
        managerRepository.save(manager1);
        saveCredentials(manager1, Role.Manager);
        Manager manager2 = new Manager("manager2", "manager2-lastname");
        managerRepository.save(manager2);
        saveCredentials(manager2, Role.Manager);

        log.info("Managers saved to database.");
    }

    private void initUser() {
        Manager manager1 = managerRepository.findById(1L).orElseThrow();
        Manager manager2 = managerRepository.findById(2L).orElseThrow();

        User user1 = new User("user1", "user1-lastname");
        user1.setManager(manager1);
        userRepository.save(user1);
        saveCredentials(user1, Role.User);
        User user2 = new User("user2", "user2-lastname");
        user2.setManager(manager2);
        userRepository.save(user2);
        saveCredentials(user2, Role.User);
        User user3 = new User("user3", "user3-lastname");
        user3.setManager(manager2);
        userRepository.save(user3);
        saveCredentials(user3, Role.User);

        log.info("Users saved to database.");
    }

    private void initProject() {
        User user1 = userRepository.findById(1L).orElseThrow();
        User user2 = userRepository.findById(2L).orElseThrow();
        User user3 = userRepository.findById(3L).orElseThrow();
        Manager manager1 = managerRepository.findById(1L).orElseThrow();
        Manager manager2 = managerRepository.findById(2L).orElseThrow();

        Project projectEmpty = new Project("project-empty");
        projectEmpty.setManager(manager1);
        projectRepository.save(projectEmpty);

        Project projectOneUser = new Project("project-1 users");
        projectOneUser.setManager(manager1);
        projectOneUser.addUser(user1);
        projectRepository.save(projectOneUser);

        Project projectTwoUser = new Project("project-2 users");
        projectTwoUser.setManager(manager2);
        projectTwoUser.addUser(user2);
        projectTwoUser.addUser(user3);
        projectRepository.save(projectTwoUser);

        log.info("Projects saved to database.");
    }

    public void initTimeChecks() {
        User user1 = userRepository.findById(1L).orElseThrow();
        User user2 = userRepository.findById(2L).orElseThrow();
        User user3 = userRepository.findById(3L).orElseThrow();
        Project project1 = projectRepository.findById(1L).orElseThrow();
        Project project2 = projectRepository.findById(2L).orElseThrow();
        Project project3 = projectRepository.findById(3L).orElseThrow();

        List<TimeCheck> timeChecks = IntStream.range(0, 6).boxed()
                .map(i -> new TimeCheck(i / 8.)).collect(Collectors.toList());

        user1.addTimeCheck(timeChecks.get(0));
        user2.addTimeCheck(timeChecks.get(1));
        user2.addTimeCheck(timeChecks.get(2));
        user3.addTimeCheck(timeChecks.get(3));
        user3.addTimeCheck(timeChecks.get(4));
        user3.addTimeCheck(timeChecks.get(5));

        project2.addTimeCheck(timeChecks.get(0));
        for (int i = 1; i < timeChecks.size(); ++i) project3.addTimeCheck(timeChecks.get(i));

        timeCheckRepository.saveAll(timeChecks);

        log.info("TimeChecks saved to database.");
    }
}
