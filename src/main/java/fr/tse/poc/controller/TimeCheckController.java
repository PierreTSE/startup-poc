package fr.tse.poc.controller;

import java.util.List;
import java.util.Map;

import javax.persistence.ManyToOne;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import fr.tse.poc.dao.ManagedRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.TimeCheckRepository;
import fr.tse.poc.domain.Project;
import fr.tse.poc.domain.TimeCheck;
import fr.tse.poc.domain.User;

@RestController
public class TimeCheckController {

	@Autowired
	private TimeCheckRepository timeRepo;
	@Autowired
	private ManagedRepository userRepo;
	@Autowired
	private ProjectRepository projectRepo;
	
	@GetMapping(path="/TimeCheck")
	public List<TimeCheck> getAll(){
		return (timeRepo.findAll());
	}
	@GetMapping(path="/TimeCheck/{id}")
	public TimeCheck getOne(@PathVariable Long id){
		return (timeRepo.getOne(id));
	}
	
	@DeleteMapping(path="/TimeCheck/{id}")
	public void delTime(@PathVariable Long id) {
		timeRepo.deleteById(id);
	}
	
	@PostMapping( path="/TimeCheck")
	public TimeCheck addTime(@RequestBody Map<String,String> params ) {
		TimeCheck nuTime = new TimeCheck( );
		nuTime.setProject(projectRepo.getOne(Long.parseLong(params.get("projectId"))));
		nuTime.setTime(Float.parseFloat(params.get("time")));
		nuTime.setUser(userRepo.getOne(Long.parseLong(params.get("UserId"))));
		return timeRepo.save(nuTime);
	}
	
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
	
}
