package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUser;
import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.ManagedRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.domain.Manager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.Collection;

@RestController
public class ManagerController {
    @Autowired
    AuthenticableUserRepository authenticableUserRepository;
    @Autowired
    private ManagedRepository managedRepository;
    @Autowired
    private ManagerRepository managerRepository;


    @GetMapping(path = "/Managers")
    public ResponseEntity<Collection<Manager>> getManagers(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());

        if (authenticableUser.getRole().equals(Role.Admin)) {
            return new ResponseEntity<>(managerRepository.findAll(), HttpStatus.OK);
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "/Managers/{id}")
    public ResponseEntity<Manager> getManagersById(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());
        switch (authenticableUser.getRole()) {
            case Admin:
            case Manager:
                return new ResponseEntity<>(this.managerRepository.getOne(id), HttpStatus.OK);
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @PostMapping(path = "/Managers")
    public ResponseEntity<Manager> addManager(@Valid @RequestBody Manager manager, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());
        switch (authenticableUser.getRole()) {
            case Admin:
                return new ResponseEntity<>(this.managerRepository.save(manager), HttpStatus.CREATED);
            case Manager:
            case User:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

    }

    @DeleteMapping(path = "/Managers/{id}")
    public void deleteManager(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());
        switch (authenticableUser.getRole()) {
            case Admin:
                managerRepository.deleteById(id);
            case Manager:
            case User:
            default:
                break;
        }

    }
}
