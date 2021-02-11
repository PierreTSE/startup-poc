package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.exceptions.UnauthorizedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class ViewController {
    @Autowired AdminRepository adminRepository;
    @Autowired ManagerRepository managerRepository;
    @Autowired UserRepository userRepository;


    @RequestMapping("/home")
    public String home(Model model, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        model.addAttribute("role", userDetails.getRole().toString());

        switch (userDetails.getRole()) {
            case Admin:
                model.addAttribute("name", adminRepository.findById(userDetails.getForeignId()).orElseThrow().getFullName());
                return "admin";
            case Manager:
                model.addAttribute("name", managerRepository.findById(userDetails.getForeignId()).orElseThrow().getFullName());
                return "manager";
            case User:
                model.addAttribute("name", userRepository.findById(userDetails.getForeignId()).orElseThrow().getFullName());
                return "user";
            default:
                throw new UnauthorizedException("Unrecognized role", userDetails);
        }
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String unauthorized(UnauthorizedException e, Model model) {
        model.addAttribute("name", e.authenticableUserDetails.getUsername());
        return "unauthorized";
    }
}
