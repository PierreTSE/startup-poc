package fr.tse.poc.controller;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;


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
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class TimeCheckController {

	@Autowired
	private TimeCheckRepository timeRepo;
	@Autowired
	private ManagedRepository userRepo;
	@Autowired
	private ProjectRepository projectRepo;
	@Autowired
	private ManagerRepository manRepo;
	
	@GetMapping(path="/TimeCheck")
	public ResponseEntity<Collection<TimeCheck>> getAll(Authentication authentication){
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		if (userDetails.getRole().equals(Role.Manager)) {
			Collection<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
            Collection<TimeCheck> allTime = timeRepo.findAll();
            Iterator<TimeCheck> i = allTime.iterator();
            while(i.hasNext()) {
            	TimeCheck last = i.next();
            	if (!managed.contains(last.getUser())) {
            		i.remove();
            	}
            }
            return new ResponseEntity<>(allTime, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
	
	
	@GetMapping(path="/TimeCheck/{id}")
	public ResponseEntity<TimeCheck> getOne(Authentication authentication, @PathVariable Long id){		
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		if (userDetails.getRole().equals(Role.Manager)) {
			Collection<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
            TimeCheck wantedTime = timeRepo.getOne(id);
            if (managed.contains(wantedTime.getUser())) {
            	 return new ResponseEntity<>(wantedTime, HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        }
		else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	/*
	@DeleteMapping(path="/TimeCheck/{id}")
	public void delTime(@PathVariable Long id) {
		timeRepo.deleteById(id);
	}
	*/
	
	@PostMapping( path="/TimeCheck")
	public ResponseEntity<TimeCheck> addTime(Authentication authentication, @RequestBody Map<String,String> params ) {
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		
		if (userDetails.getRole().equals(Role.User)) {
		
			TimeCheck nuTime = new TimeCheck( );
			List<Project> projs = projectRepo.findAll();
			int it = 0;
			while (it<projs.size() && (params.get("name" ) !=(projs.get(it).getName()) ) )  {
				it++;
			}
			if (it == projs.size()) {
				return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
			}
			else {				
				nuTime.setProject(projs.get(it));
				nuTime.setTime(Float.parseFloat(params.get("time")));
				nuTime.setUser(userRepo.getOne(userDetails.getForeignId()));
				return new ResponseEntity<>(timeRepo.save(nuTime),HttpStatus.OK);
			}	
		}
		else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	/*
	@PatchMapping(path="/TimeCheck/{id}")
	public void modTime(@PathVariable Long id, @RequestBody Map<String,String> params) {
		TimeCheck myTime = timeRepo.getOne(id);
		
		if (params.get("projectId") != null){
			myTime.setProject(projectRepo.getOne(Long.parseLong(params.get("projectId"))));
		}
		if (params.get("time") != null) {
			myTime.setTime(Float.parseFloat(params.get("time")));
		}
		if (params.get("UserId")!= null) {
			myTime.setUser(userRepo.getOne(Long.parseLong(params.get("UserId"))));
		}
	}
	*/
	
}
