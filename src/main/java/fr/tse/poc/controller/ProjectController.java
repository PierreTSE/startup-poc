package fr.tse.poc.controller;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.ManagedRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.TimeCheckRepository;
import fr.tse.poc.domain.Project;
import fr.tse.poc.domain.TimeCheck;
import fr.tse.poc.domain.User;

@RestController
public class ProjectController {

	@Autowired
	private ProjectRepository repo;
	
	@Autowired
	private ManagerRepository manRepo;
	
	@Autowired
	private TimeCheckRepository timeRepo;
	
	@Autowired
	private ManagedRepository userRepo;
	
	/*
	 * Returns all projects
	 */
	@GetMapping(path="/projects")
	public ResponseEntity<Collection<Project>> getProjects(Authentication authentication){
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		
		switch(userDetails.getRole()) {
		case Admin:
			return new ResponseEntity<>(repo.findAll(),HttpStatus.OK);
		case Manager:
			return new ResponseEntity<>( manRepo.getOne(userDetails.getForeignId()).getProjects(),HttpStatus.OK);
		case User : 
			ArrayList<Project> projs = new ArrayList<Project>();
			projs.add( userRepo.getOne(userDetails.getForeignId()).getProject());
			return new ResponseEntity<>(projs,HttpStatus.OK);
			default: return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	/*
	 * return the project given by the id
	 */
	@GetMapping(path="/projects/{id}")
	public  ResponseEntity<Project> getProjectById(@PathVariable long id, Authentication authentication){
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		
		switch(userDetails.getRole()) {
		case Admin:
			return new ResponseEntity<>(repo.getOne(id),HttpStatus.OK);
		case Manager:
			Project theProj = repo.getOne(id);
			if ( manRepo.getOne(userDetails.getForeignId()).getProjects().contains(theProj)) {
				return new ResponseEntity<>( theProj,HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		case User :
			if (userRepo.getOne(userDetails.getForeignId()).getProject().getId() == id) {
				return new ResponseEntity<>(repo.getOne(id),HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		default: return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	// todo @delete
	/*
	@DeleteMapping(path="/Projects/{id}")
	public void delProj(@PathVariable long id) {
		repo.deleteById(id);
	}
	*/
	
	/*
	 * Add a new project With a name and a manager
	 */
	@PostMapping(path="/projects")
	public ResponseEntity<Project> addProject(Authentication authentication, @RequestBody Map<String,String> params ){
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
	//todo admin
		if (userDetails.getRole().equals(Role.Manager)) {
			// todo getOne -> findbyId avec try-catch et 404 si pas trouvé
			Project pro = new Project(params.get("name"), manRepo.getOne( userDetails.getForeignId() ));
			return new ResponseEntity<>(repo.save(pro),HttpStatus.OK);
		}
		else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
		
		
	}
	
	/*
	 * modify a project name or manager given by id
	 */
	@PatchMapping(path="/projects/{id}")
	public ResponseEntity<Project> modProjectBase(@PathVariable long id, @RequestBody Map<String,String> params, Authentication authentication) {
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		//todo admin
		if (userDetails.getRole().equals(Role.Manager)) {
			Project pro = repo.getOne(id);	
			if (pro.getManager() ==  manRepo.getOne(userDetails.getForeignId())){
				pro.setName(params.get("name"));
				pro.setManager(manRepo.getOne( Long.parseLong( params.get("manId") ) ) );
				return new ResponseEntity<>(pro, HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

			}
		}
		else{
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} 
	}
	
	
	/*
	 * modify a project user list by adding or deleting the list in the  body depending on the boolean add.
	 */
	@PatchMapping(path= "/projects/{id}/users")
	// todo voir si List<Long> se fait
	// todo Boolean add
	public ResponseEntity<Project> modProjectUsers(Authentication authentication, @PathVariable long id, @RequestBody List<String> users, String add) {
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		//todo admin
		if (userDetails.getRole().equals(Role.Manager)) {

			Project myProj = repo.getOne(id);
			if (myProj.getManager() ==  manRepo.getOne(userDetails.getForeignId())){

				Set<User> params = getUserFromStringList(users);
				if (Boolean.parseBoolean(add)) {
					myProj.getUsers().addAll(params);
				}
				else {
					myProj.getUsers().removeAll(params);
				}
				return new ResponseEntity<>(myProj,HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		}
		else{
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		} 	
	}

	// todo -> UserRepo
	private Set<User> getUserFromStringList(List<String> ids){
		Set<User> users = new HashSet<User>();
		ids.forEach(tId -> { 
			users.add(userRepo.getOne(Long.parseLong(tId)) );
		});
		
		return users;
	}
}
