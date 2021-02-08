package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.AdminRepository;
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


    @RequestMapping("/home")
    public String home(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Admin:
                return "admin";
            case Manager:
                return "manager";
            case User:
                return "user";
            default:
                throw new UnauthorizedException("Unrecognized role", userDetails);
        }
    }

    @RequestMapping("/admin")
    public String admin(Model model, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        if (userDetails.getRole() != Role.Admin) {
            throw new UnauthorizedException("Only Admin role is authorized.", userDetails);
        }
        model.addAttribute("role", userDetails.getRole().toString());
        model.addAttribute("name", adminRepository.findById(userDetails.getForeignId()).orElseThrow().getFullName());
        return "admin";
    }

    @RequestMapping("/manager")
    public String manager(Model model, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        if (userDetails.getRole() != Role.Manager) {
            throw new UnauthorizedException("Only Manager role is authorized.", userDetails);
        }
        return "manager";
    }

    @RequestMapping("/user")
    public String user(Model model, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        if (userDetails.getRole() != Role.User) {
            throw new UnauthorizedException("Only User role is authorized.", userDetails);
        }
        return "user";
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String unauthorized(UnauthorizedException e, Model model) {
        model.addAttribute("name", e.authenticableUserDetails.getUsername());
        return "unauthorized";
    }
}
