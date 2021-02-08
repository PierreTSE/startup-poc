package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.domain.Manager;
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
public class ManagerController {
    @Autowired
    ManagerRepository managerRepository;

    @PostMapping(path = "/managers")
    public ResponseEntity<Manager> addManager(@Valid @RequestBody Manager manager, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Admin:
                return new ResponseEntity<>(managerRepository.save(manager), HttpStatus.CREATED);
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
