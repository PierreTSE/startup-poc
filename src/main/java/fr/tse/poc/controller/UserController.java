package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;
import java.util.NoSuchElementException;

@Slf4j
@RestController
public class UserController {
    @Autowired
    ManagerRepository managerRepository;
    @Autowired
    UserRepository userRepository;

    @GetMapping(path = "/users")
    public ResponseEntity<Collection<User>> getUsers(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        if (userDetails.getRole().equals(Role.Admin)) {
            return new ResponseEntity<>(userRepository.findAll(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "/users/{id}")
    public ResponseEntity<User> getUserById(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        User user = null;
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

    @PostMapping(path = "/users")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Admin:
                return new ResponseEntity<>(userRepository.save(user), HttpStatus.CREATED);
            case Manager:
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping(path = "/users/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        User user = null;
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
