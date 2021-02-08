package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Admin;
import fr.tse.poc.domain.Manager;
import fr.tse.poc.domain.People;
import fr.tse.poc.domain.User;
import fr.tse.poc.service.UserService;
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
	@Autowired
	ManagerRepository managerRepository;
	@Autowired
	UserRepository userRepository;
	@Autowired
	AdminRepository adminRepository;
	@Autowired UserService userService;

	@PostMapping(path = "/users")
	public ResponseEntity<User> addUser(@RequestBody User user, Authentication authentication) {
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

		Manager manager = null;

		switch (userDetails.getRole()) {
			case Manager:
				// Put authentified manager as this user's manager
				manager = managerRepository.getOne(userDetails.getForeignId());
				user.setManager(manager);
				return new ResponseEntity<>(userRepository.save(user), HttpStatus.CREATED);
			case Admin:
				return new ResponseEntity<>(userRepository.save(user), HttpStatus.CREATED);
			case User:
			default:
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

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

	@PatchMapping(path = "/users/{id}")
	public ResponseEntity<People> updateUser(@PathVariable long id,
											 @RequestBody Map<String, String> params,
											 Authentication authentication) {
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		// find user
		User user = null;
		try {
			user = userRepository.findById(id).orElseThrow();
		} catch (NoSuchElementException e) {
			log.error(e.getMessage());
			return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
		}
		if (params.containsKey("status")) {
			switch (params.get("status")) {
				case "Admin":
					userRepository.deleteById(user.getId());
					Admin newAdmin = new Admin(user.getFirstname(), user.getLastname());
					return new ResponseEntity<>(adminRepository.save(newAdmin), HttpStatus.NO_CONTENT);
				case "Manager":
					userRepository.deleteById(user.getId());
					Manager newManager = new Manager(user.getFirstname(), user.getLastname());
					return new ResponseEntity<>(managerRepository.save(newManager), HttpStatus.NO_CONTENT);
				default:
					return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
			}
		} else if (params.containsKey("manager")) {
			// change user's manager
			Manager newManager = null;
			try {
				newManager = managerRepository.findById(parseLong(params.get("manager"))).orElseThrow();
			} catch (NoSuchElementException e) {
				log.error(e.getMessage());
				return new ResponseEntity<>(userRepository.save(user), HttpStatus.BAD_REQUEST);
			}

			switch (userDetails.getRole()) {
				case Admin:
					user.setManager(newManager);
					return new ResponseEntity<>(userRepository.save(user), HttpStatus.OK);
				case Manager:
				case User:
				default:
					return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		} else {
			return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
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
