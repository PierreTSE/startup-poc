package fr.tse.poc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.AuthenticableUserDetailsService;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

import static java.lang.Long.parseLong;

@Slf4j
@RestController
public class UserController {
    final private ObjectMapper mapper = new ObjectMapper();
    @Autowired private AdminRepository adminRepository;
    @Autowired private ManagerRepository managerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private ProjectRepository projectRepository;
    @Autowired private AuthenticableUserDetailsService authenticableUserDetailsService;
    @Autowired private AuthenticableUserRepository authenticableUserRepository;

    @PostMapping(path = "/users")
    public ResponseEntity<User> addUser(@RequestBody Map<String, Object> body, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        Manager manager;
        User user = mapper.convertValue(body.get("user"), User.class);

        switch (userDetails.getRole()) {
            case Manager:
                // Put authentified manager as this user's manager
                manager = managerRepository.findById(userDetails.getForeignId()).orElseThrow();
                user.setManager(manager);
                if(body.containsKey("projectID")) {
                    try {
                        user.getProjects().add(projectRepository.findById(Long.valueOf((String) body.get("projectID"))).orElseThrow());
                    } catch (NoSuchElementException e) {
                        log.error(e.getMessage());
                        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                    }
                }
                user = userRepository.save(user);
                authenticableUserDetailsService.addAuthenticableUser(user, Role.User, (String) body.get("password"), true);
                return new ResponseEntity<>(user, HttpStatus.CREATED);
            case Admin:
                try {
                    manager = managerRepository.findById(Long.valueOf((String) body.get("managerID"))).orElseThrow();
                } catch (NoSuchElementException e) {
                    log.error(e.getMessage());
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
                user.setManager(manager);
                user = userRepository.save(user);
                authenticableUserDetailsService.addAuthenticableUser(user, Role.User, (String) body.get("password"), true);
                return new ResponseEntity<>(user, HttpStatus.CREATED);
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "/users")
    public ResponseEntity<Collection<User>> getUsers(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Admin:
                return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
            case Manager:
                Manager manager;
                try {
                    manager = managerRepository.findById(userDetails.getForeignId()).orElseThrow();
                } catch (NoSuchElementException e) {
                    log.error(e.getMessage());
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
                return new ResponseEntity<>(manager.getUsers(), HttpStatus.OK);
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        User user;
        try {
            user = userRepository.findById(id).orElseThrow();
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        switch (userDetails.getRole()) {
            case Admin:
                return new ResponseEntity<>(user, HttpStatus.OK);
            case Manager:
                if (managerRepository.getOne(userDetails.getForeignId()).getUsers().contains(user)) {
                    return new ResponseEntity<>(user, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PatchMapping(path = "/users/{id}")
    public ResponseEntity<People> updateUser(@PathVariable long id,
                                             @RequestBody Map<String, String> params,
                                             Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Admin:
            case Manager:
                break;
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // find user
        User user;
        try {
            user = userRepository.findById(id).orElseThrow();
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        String password = authenticableUserRepository.findByUsername(user.getFirstname()).getPassword();

        if (params.containsKey("status") && userDetails.getRole().equals(Role.Admin)) {
            switch (params.get("status") ) {
                case "Admin":
                    userRepository.deleteById(user.getId());
                    Admin newAdmin = adminRepository.save(new Admin(user.getFirstname(), user.getLastname()));
                    authenticableUserRepository.deleteById(user.getFirstname());
                    authenticableUserDetailsService.addAuthenticableUser(newAdmin, Role.Admin, password, false);
                    return new ResponseEntity<>(newAdmin, HttpStatus.NO_CONTENT);
                case "Manager":
                    userRepository.deleteById(user.getId());
                    Manager newManager = managerRepository.save(new Manager(user.getFirstname(), user.getLastname()));
                    authenticableUserRepository.deleteById(user.getFirstname());
                    authenticableUserDetailsService.addAuthenticableUser(newManager, Role.Manager, password, false);
                    return new ResponseEntity<>(newManager, HttpStatus.NO_CONTENT);
                default:
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        } else if (params.containsKey("manager") && userDetails.getRole().equals(Role.Admin)) {
            // change user's manager
            Manager newManager;
            try {
                newManager = managerRepository.findById(parseLong(params.get("manager"))).orElseThrow();
            } catch (NoSuchElementException e) {
                log.error(e.getMessage());
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            user.setManager(newManager);
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        } else if (params.containsKey("project") && userDetails.getRole().equals(Role.Manager)) {
            // add project to user
            Project proj=null;
            try {
                proj = projectRepository.findById(parseLong(params.get("project"))).orElseThrow();
            } catch (NoSuchElementException e) {
                log.error(e.getMessage());
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
            proj.addUser(user);
            projectRepository.save(proj);
            return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
        }else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(path = "/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        User user;
        try {
            user = userRepository.findById(id).orElseThrow();
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }

        switch (userDetails.getRole()) {
            case Admin:
                userRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            case Manager:
                if (managerRepository.getOne(userDetails.getForeignId()).getUsers().contains(user)) {
                    userRepository.deleteById(id);
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
