package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUser;
import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.ManagedRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.domain.People;
import fr.tse.poc.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

public class UserController {
    @Autowired
    AuthenticableUserRepository authenticableUserRepository;
    @Autowired
    private ManagedRepository managedRepository;
    @Autowired
    private ManagerRepository managerRepository;

    /**
     * Find all users
     *
     * @param authentication
     *
     * @return
     */

    @GetMapping(path = "/Users")
    public ResponseEntity<Collection<User>> getUsers(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());

        if (authenticableUser.getRole().equals(Role.Admin)) {
            return new ResponseEntity<>(this.managedRepository.findAll(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "/Users/{id}")
    public ResponseEntity<User> getUsersById(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());
        People user = managedRepository.getOne(id);
        switch (authenticableUser.getRole()) {
            case Admin:
                return new ResponseEntity(user, HttpStatus.OK);
            case Manager:
                if (managerRepository.getOne(authenticableUser.getForeignID()).getUsers().contains(user)) {
                    return new ResponseEntity(user, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(path = "/Users")
    public ResponseEntity<User> addUser(@Valid @RequestBody User user, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());
        switch (authenticableUser.getRole()) {
            case Admin:
                return new ResponseEntity<>(this.managedRepository.save(user), HttpStatus.CREATED);
            case Manager:
                if (managerRepository.getOne(authenticableUser.getForeignID()).getUsers().contains(user)) {
                    return new ResponseEntity<>(this.managedRepository.save(user), HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

    }

    @DeleteMapping(path = "/Users/{id}")
    public void deleteUser(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());
        People user = managedRepository.getOne(id);
        switch (authenticableUser.getRole()) {
            case Admin:
                this.managedRepository.deleteById(id);
            case Manager:
                if (managerRepository.getOne(authenticableUser.getForeignID()).getUsers().contains(user)) {
                    this.managedRepository.deleteById(id);
                }
            case User:
            default:
                break;
        }

    }

}
