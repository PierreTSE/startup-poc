package fr.tse.poc.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.AuthenticableUserDetailsService;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Admin;
import fr.tse.poc.domain.Manager;
import fr.tse.poc.domain.People;
import fr.tse.poc.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;

@Slf4j
@RestController
public class ManagerController {
    final private ObjectMapper mapper = new ObjectMapper();
    @Autowired private AdminRepository adminRepository;
    @Autowired private ManagerRepository managerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private AuthenticableUserDetailsService authenticableUserDetailsService;
    @Autowired private AuthenticableUserRepository authenticableUserRepository;

    @PostMapping(path = "/managers")
    public ResponseEntity<Manager> addManager(@RequestBody Map<String, Object> body, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        Manager manager = mapper.convertValue(body.get("manager"), Manager.class);

        switch (userDetails.getRole()) {
            case Admin:
                manager = managerRepository.save(manager);
                authenticableUserDetailsService.addAuthenticableUser(manager, Role.Manager, (String) body.get("password"), true);
                return new ResponseEntity<>(manager, HttpStatus.CREATED);
            case Manager:
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "/managers")
    public ResponseEntity<Collection<Manager>> getManagers(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        if (userDetails.getRole().equals(Role.Admin)) {
            return new ResponseEntity<>(managerRepository.findAll(), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @GetMapping(path = "/managers/{id}")
    public ResponseEntity<Manager> getManagerById(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Admin:
            case Manager:
                try {
                    return new ResponseEntity<>(managerRepository.findById(id).orElseThrow(), HttpStatus.OK);
                } catch (NoSuchElementException e) {
                    log.error(e.getMessage());
                    return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                }
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PatchMapping(path = "/managers/{id}")
    public ResponseEntity<People> updateUser(@PathVariable long id,
                                             @RequestBody Map<String, String> params,
                                             Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Admin:
                break;
            case Manager:
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        // find manager
        Manager manager;
        try {
            manager = managerRepository.findById(id).orElseThrow();
        } catch (NoSuchElementException e) {
            log.error(e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        String password = authenticableUserRepository.findByUsername(manager.getFirstname()).getPassword();

        if (params.containsKey("status")) {
            switch (params.get("status")) {
                case "Admin":
                    managerRepository.deleteById(manager.getId());
                    Admin newAdmin = adminRepository.save(new Admin(manager.getFirstname(), manager.getLastname()));
                    authenticableUserRepository.deleteById(manager.getFirstname());
                    authenticableUserDetailsService.addAuthenticableUser(newAdmin, Role.Admin, password, false);
                    return new ResponseEntity<>(newAdmin, HttpStatus.NO_CONTENT);
                case "User":
                    managerRepository.deleteById(manager.getId());
                    User newUser = userRepository.save(new User(manager.getFirstname(), manager.getLastname()));
                    authenticableUserRepository.deleteById(manager.getFirstname());
                    authenticableUserDetailsService.addAuthenticableUser(newUser, Role.User, password, false);
                    return new ResponseEntity<>(newUser, HttpStatus.NO_CONTENT);
                default:
                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping(path = "/managers/{id}")
    public ResponseEntity<Void> deleteManager(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Admin:
                managerRepository.deleteById(id);
                return new ResponseEntity<>(HttpStatus.OK);
            case Manager:
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
