package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUser;
import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.AuthenticableUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class CredentialsController {
    private final BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
    @Autowired AuthenticableUserRepository authenticableUserRepository;

    @GetMapping(path = "/credentials")
    public Map<String, Object> getCredentials(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        return Map.of(
                "username", userDetails.getUsername(),
                "password", userDetails.getPassword(),
                "role", userDetails.getRole().toString(),
                "foreignID", userDetails.getForeignId().toString()
        );
    }

    @PutMapping(path = "/credentials")
    public void updateCredentials(@RequestParam String password, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        AuthenticableUser authenticableUser = authenticableUserRepository.findByUsername(userDetails.getUsername());
        authenticableUser.setPassword(bCryptPasswordEncoder.encode(password));
        authenticableUserRepository.save(authenticableUser);
    }
}
