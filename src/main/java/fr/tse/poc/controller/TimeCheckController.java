package fr.tse.poc.controller;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
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
import fr.tse.poc.utils.PDFGenerator;
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
			allTime.removeIf(last -> !managed.contains(last.getUser()));
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

	
	
	@DeleteMapping(path="/TimeCheck/{id}")
	public ResponseEntity<TimeCheck> delTime(Authentication authentication,@PathVariable Long id) {
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

		switch(userDetails.getRole()) {
		case Admin:
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		case Manager:
			Collection<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
            TimeCheck wantedTime = timeRepo.getOne(id);
            if (managed.contains(wantedTime.getUser())) {
            	timeRepo.deleteById(id);
            	return new ResponseEntity<>( HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
		case User :
			if (userRepo.getOne(userDetails.getForeignId()) == timeRepo.getOne(id).getUser()) {
				timeRepo.deleteById(id);
				return new ResponseEntity<>(HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		default: return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	

	@PostMapping( path="/TimeCheck")
	public ResponseEntity<TimeCheck> addTime( @RequestPart("projectId") long projectId, @RequestPart("time") float time,Authentication authentication ) {
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

		if (userDetails.getRole().equals(Role.User)) {
		
			TimeCheck nuTime = new TimeCheck( );
			Optional<Project> proj = projectRepo.findById(projectId);
			if (proj.isPresent()) {
				nuTime.setProject(proj.get());
				nuTime.setTime(time);
				nuTime.setUser(userRepo.getOne(userDetails.getForeignId()));
				return new ResponseEntity<>(timeRepo.save(nuTime),HttpStatus.OK);
			}
			else {				
				return new ResponseEntity<>(HttpStatus.NOT_FOUND);

			}	
		}
		else {
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}

	
	
	@PatchMapping(path="/TimeCheck/{id}")
	public ResponseEntity<TimeCheck> modTime(Authentication authentication,@PathVariable Long id, @RequestBody Map<String,String> params) {
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

		switch(userDetails.getRole()) {
		case Admin:
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		case Manager:
			Collection<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
            TimeCheck wantedTime = timeRepo.getOne(id);
            if (managed.contains(wantedTime.getUser())) {
            	TimeCheck myTime = timeRepo.getOne(id);
        		
        		if (params.get("projectId") != null){
        			Optional<Project> proj = projectRepo.findById(Long.parseLong(params.get("projectId")));
        			
        			if (proj.isEmpty()){
                    	return new ResponseEntity<>( HttpStatus.NOT_FOUND);
        			}
        			myTime.setProject(proj.get());
        		}
        		if (params.get("time") != null) {
        			myTime.setTime(Float.parseFloat(params.get("time")));
        		}
            	return new ResponseEntity<>(timeRepo.save(myTime), HttpStatus.OK);
            }
            else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
		case User :
			if (userRepo.getOne(userDetails.getForeignId()) == timeRepo.getOne(id).getUser()) {
				
				Optional<TimeCheck> myTimeObj = timeRepo.findById(id);
				
				if (myTimeObj.isEmpty()) {
					return new ResponseEntity<>( HttpStatus.NOT_FOUND);
				}
				TimeCheck myTime = myTimeObj.get();
				
				
				if (params.get("projectId") != null){
					myTime.setProject(projectRepo.getOne(Long.parseLong(params.get("projectId"))));
				}
				if (params.get("time") != null) {
					myTime.setTime(Float.parseFloat(params.get("time")));
				}
				if (params.get("UserId")!= null) {
					myTime.setUser(userRepo.getOne(Long.parseLong(params.get("UserId"))));
				}
				return new ResponseEntity<>(timeRepo.save(myTime), HttpStatus.OK);
			}
			else {
				return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
			}
		default: return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		}
	}
	
	/**
	 * Create and download a pdf file containing timestamps
	 * if called by a user it send all them timestamps
	 * if called by a manager, either :
	 * the body of the request contains a list of user the their timestamps are sent
	 * there is no body and timestamps form all managed users are sent
	 * @param usersId
	 * @param authentication
	 * @return
	 */
	@GetMapping(path="/TimeCheck/export")
	public ResponseEntity<Resource> ExportPdf(@RequestPart(value ="users") Optional<List<Long>> usersId, Authentication authentication){		
		AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
		PDFGenerator pDFGenerator = new PDFGenerator();

		switch(userDetails.getRole()) {
		case Admin:
			return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		case Manager:
			HashSet<TimeCheck> time = new HashSet<TimeCheck>();
			Set<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
			if (usersId.isPresent()) {
			for (Long id : usersId.get()) {
				User myUser =  userRepo.getOne(id);
				if (managed.contains(myUser)) {
					time.addAll(myUser.getTimeChecks());
				}
			}
			}
			else {
				for (User user : managed ) {
					time.addAll(user.getTimeChecks());
					
				}
			}

			String filePath  = pDFGenerator.generatePdfReport(time);
			
			Path path = Paths.get(filePath);
			Resource resource = null;
			try {
				resource = new UrlResource(path.toUri());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("application/pdf"))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);

		case User :
			Set<TimeCheck> timeUser =  (userRepo.getOne(userDetails.getForeignId())).getTimeChecks();
			
			filePath  = pDFGenerator.generatePdfReport(timeUser);
			
			path = Paths.get(filePath);
			resource = null;
			try {
				resource = new UrlResource(path.toUri());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
			return ResponseEntity.ok()
					.contentType(MediaType.parseMediaType("application/pdf"))
					.header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + resource.getFilename() + "\"")
					.body(resource);
			
		
		default: new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
		
		}
		return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
	}
	
	
}
