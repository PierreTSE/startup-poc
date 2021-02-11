package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.TimeCheckRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Project;
import fr.tse.poc.domain.TimeCheck;
import fr.tse.poc.domain.User;
import fr.tse.poc.utils.PDFGenerator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

@Slf4j
@RestController
public class TimeCheckController {

    @Autowired private TimeCheckRepository timeRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private ProjectRepository projectRepo;
    @Autowired private ManagerRepository manRepo;

    @PostMapping(path = "/timecheck")
    public ResponseEntity<TimeCheck> addTime(@RequestBody Map<String, String> params, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        if (params.get("projectId") == null || params.get("time") == null) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Long projectId = Long.valueOf(params.get("projectId"));
        float time = Float.parseFloat(params.get("time"));

        if (userDetails.getRole().equals(Role.User)) {

            TimeCheck nuTime = new TimeCheck();
            Optional<Project> proj = projectRepo.findById(projectId);
            if (proj.isPresent()) {
                nuTime.setProject(proj.get());
                nuTime.setTime(time);
                nuTime.setUser(userRepo.getOne(userDetails.getForeignId()));
                return new ResponseEntity<>(timeRepo.save(nuTime), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);

            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "/timecheck")
    public ResponseEntity<Collection<TimeCheck>> getTimeChecks(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        switch (userDetails.getRole()) {
            case Manager:
                Collection<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
                Collection<TimeCheck> allTime = timeRepo.findAll();
                allTime.removeIf(last -> !managed.contains(last.getUser()));
                return new ResponseEntity<>(allTime, HttpStatus.OK);
            case User:
                User user = userRepo.getOne(userDetails.getForeignId());
                Collection<TimeCheck> TimeUser = user.getTimeChecks();

                return new ResponseEntity<>(TimeUser, HttpStatus.OK);
            case Admin:

            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @GetMapping(path = "/timecheck/{id}")
    public ResponseEntity<TimeCheck> getTimeCheck(Authentication authentication, @PathVariable Long id) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        switch (userDetails.getRole()) {
            case Manager:
                Collection<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
                TimeCheck wantedTime = timeRepo.getOne(id);
                if (managed.contains(wantedTime.getUser())) {
                    return new ResponseEntity<>(wantedTime, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
                wantedTime = timeRepo.getOne(id);
                if (userRepo.getOne(userDetails.getForeignId()) == wantedTime.getUser()) {
                    return new ResponseEntity<>(wantedTime, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case Admin:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Create and download a pdf file containing timestamps
     * if called by a user it send all them timestamps
     * if called by a manager, either :
     * the body of the request contains a list of user the their timestamps are sent
     * there is no body and timestamps form all managed users are sent
     * <p>
     * Optional values startDate & endDate to select within a time range
     */
    @GetMapping(path = "/timecheck/export")
    public ResponseEntity<Resource> exportPDF(@RequestPart(value = "users") Optional<List<Long>> usersId, @RequestPart(value = "startDate") Optional<Float> startDate, @RequestPart(value = "endDate") Optional<Float> endDate, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        PDFGenerator pDFGenerator = new PDFGenerator();

        switch (userDetails.getRole()) {
            case Admin:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            case Manager:
                HashSet<TimeCheck> time = new HashSet<TimeCheck>();
                Set<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
                if (usersId.isPresent()) {
                    for (Long id : usersId.get()) {
                        User myUser = userRepo.getOne(id);
                        if (managed.contains(myUser)) {
                            Set<TimeCheck> userTime = new HashSet<>(myUser.getTimeChecks());
                            // must be called before you can call i.remove()
                            startDate.ifPresent(aFloat -> userTime.removeIf(timei -> timei.getTime() < aFloat));
                            // must be called before you can call i.remove()
                            endDate.ifPresent(aFloat -> userTime.removeIf(timei -> timei.getTime() < aFloat));
                            time.addAll(userTime);
                        }
                    }
                } else {
                    for (User user : managed) {

                        Set<TimeCheck> userTime = new HashSet<>(user.getTimeChecks());
                        // must be called before you can call i.remove()
                        startDate.ifPresent(aFloat -> userTime.removeIf(timei -> timei.getTime() < aFloat));
                        // must be called before you can call i.remove()
                        endDate.ifPresent(aFloat -> userTime.removeIf(timei -> timei.getTime() < aFloat));
                        time.addAll(userTime);
                    }
                }

                String filePath = pDFGenerator.generatePdfReport(time);

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

            case User:
                Set<TimeCheck> timeUser = new HashSet<>(userRepo.getOne(userDetails.getForeignId()).getTimeChecks());
                // must be called before you can call i.remove()
                startDate.ifPresent(aFloat -> timeUser.removeIf(timei -> timei.getTime() < aFloat));
                // must be called before you can call i.remove()
                endDate.ifPresent(aFloat -> timeUser.removeIf(timei -> timei.getTime() < aFloat));
                filePath = pDFGenerator.generatePdfReport(timeUser);

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

            default:
                new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

        }
        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
    }

    @PatchMapping(path = "/timecheck/{id}")
    public ResponseEntity<TimeCheck> updateTimeCheck(Authentication authentication, @PathVariable Long id, @RequestBody Map<String, String> params) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        switch (userDetails.getRole()) {
            case Manager:
                Collection<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
                TimeCheck wantedTime = timeRepo.getOne(id);
                if (managed.contains(wantedTime.getUser())) {
                    TimeCheck myTime = timeRepo.getOne(id);

                    if (params.get("projectId") != null) {
                        Optional<Project> proj = projectRepo.findById(Long.parseLong(params.get("projectId")));

                        if (proj.isEmpty()) {
                            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                        }
                        myTime.setProject(proj.get());
                    }
                    if (params.get("time") != null) {
                        myTime.setTime(Float.parseFloat(params.get("time")));
                    }
                    return new ResponseEntity<>(timeRepo.save(myTime), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
                if (userRepo.getOne(userDetails.getForeignId()) == timeRepo.getOne(id).getUser()) {

                    Optional<TimeCheck> myTimeObj = timeRepo.findById(id);

                    if (myTimeObj.isEmpty()) {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                    TimeCheck myTime = myTimeObj.get();


                    if (params.get("projectId") != null) {
                        myTime.setProject(projectRepo.getOne(Long.parseLong(params.get("projectId"))));
                    }
                    if (params.get("time") != null) {
                        myTime.setTime(Float.parseFloat(params.get("time")));
                    }
                    if (params.get("UserId") != null) {
                        myTime.setUser(userRepo.getOne(Long.parseLong(params.get("UserId"))));
                    }
                    return new ResponseEntity<>(timeRepo.save(myTime), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case Admin:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping(path = "/timecheck/{id}")
    public ResponseEntity<TimeCheck> deleteTimeCheck(Authentication authentication, @PathVariable Long id) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        switch (userDetails.getRole()) {
            case Manager:
                Collection<User> managed = manRepo.getOne(userDetails.getForeignId()).getUsers();
                TimeCheck wantedTime = timeRepo.getOne(id);
                if (managed.contains(wantedTime.getUser())) {
                    timeRepo.deleteById(id);
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
                if (userRepo.getOne(userDetails.getForeignId()) == timeRepo.getOne(id).getUser()) {
                    timeRepo.deleteById(id);
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case Admin:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
