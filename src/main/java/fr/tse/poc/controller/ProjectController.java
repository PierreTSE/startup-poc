package fr.tse.poc.controller;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

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
	private ManagerRepository ManRepo;
	
	@Autowired
	private TimeCheckRepository timeRepo;
	
	@Autowired
	private ManagedRepository userRepo;
	
	/*
	 * Returns all projects
	 */
	@GetMapping(path="/Projects")
	public List<Project> getProject(){
		
		return repo.findAll();
		
	}
	
	/*
	 * return the project given by the id
	 */
	@GetMapping(path="/Projects/{id}")
	public Project getOneProject(@PathVariable long id){
		return repo.getOne(id);
	}
	
	/*
	 * Delete the project given by the id
	 */
	@DeleteMapping(path="/Projects/{id}")
	public void delProj(@PathVariable long id) {
		repo.deleteById(id);
	}
	
	/*
	 * Add a new project With a name and a manager
	 */
	@PostMapping(path="/Project")
	public Project addProject(@RequestBody Map<String,String> params ){

		Project pro = new Project();
		pro.setName(params.get("name"));
		pro.setManager(ManRepo.getOne( Long.parseLong( params.get("manId") ) ) );
		pro.setTimeChecks(new HashSet<TimeCheck>());
		pro.setUsers(new HashSet<User>());
		return repo.save(pro);
	}
	
	/*
	 * modify a project name or manager given by id
	 */
	@PatchMapping(path="/Project/{id}/mod")
	public Project modProjectBase(@PathVariable long id, @RequestBody Map<String,String> params) {

		Project pro = repo.getOne(id);
		pro.setName(params.get("name"));
		pro.setManager(ManRepo.getOne( Long.parseLong( params.get("manId") ) ) );
		return pro;
	}
	
	/*
	 * modify a project timeCheck list by adding or deleting the list in the  body depending on the boolean add.
	 */
	@PatchMapping(path= "/Project/{id}/timeCheck")
	public Project modProjectTime(@PathVariable long id, @RequestBody List<String> timesId, String add) {
		Project myProj = repo.getOne(id);
		Set<TimeCheck> params = getTimeFromStringList(timesId);
		
		if (Boolean.parseBoolean(add)) {
			myProj.getTimeChecks().addAll(params);
		}
		else {
			myProj.getTimeChecks().removeAll(params);
		}
		return myProj;
	}
	
	/*
	 * modify a project user list by adding or deleting the list in the  body depending on the boolean add.
	 */
	@PatchMapping(path= "/Project/{id}/managed")
	public Project modProjectUsers(@PathVariable long id, @RequestBody List<String> users, String add) {
		Project myProj = repo.getOne(id);
		Set<User> params = getUserFromStringList(users);
		
		if (Boolean.parseBoolean(add)) {
			myProj.getUsers().addAll(params);
		}
		else {
			myProj.getUsers().removeAll(params);
		}
		return myProj;		
	}
	
	
	private Set<TimeCheck> getTimeFromStringList(List<String> ids){
		Set<TimeCheck> timeCheck = new HashSet<TimeCheck>();
		ids.forEach(tId -> { 
			timeCheck.add(timeRepo.getOne(Long.parseLong(tId)) );
		});
		
		return timeCheck;
	}

	private Set<User> getUserFromStringList(List<String> ids){
		Set<User> users = new HashSet<User>();
		ids.forEach(tId -> { 
			users.add(userRepo.getOne(Long.parseLong(tId)) );
		});
		
		return users;
	}
}
