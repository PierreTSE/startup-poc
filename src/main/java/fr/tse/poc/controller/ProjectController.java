package fr.tse.poc.controller;

import fr.tse.poc.authentication.AuthenticableUserDetails;
import fr.tse.poc.authentication.Role;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.TimeCheckRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Manager;
import fr.tse.poc.domain.Project;
import fr.tse.poc.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
public class ProjectController {
    @Autowired private ProjectRepository repo;

    @Autowired private ManagerRepository manRepo;
    @Autowired private TimeCheckRepository timeRepo;
    @Autowired private UserRepository userRepo;

    /**
     * Add a new project with a name and a manager.
     */
    @PostMapping(path = "/projects")
    public ResponseEntity<Project> addProject(@RequestBody String projectName, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        if (userDetails.getRole().equals(Role.Manager)) {

            Optional<Manager> man = manRepo.findById(userDetails.getForeignId());

            if (man.isPresent()) {
                Project pro = new Project(projectName);
                pro.setManager(man.get());
                return new ResponseEntity<>(repo.save(pro), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }


        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Return all projects.
     */
    @GetMapping(path = "/projects")
    public ResponseEntity<Collection<Project>> getProjects(Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        switch (userDetails.getRole()) {
            case Manager:
                return new ResponseEntity<>(manRepo.getOne(userDetails.getForeignId()).getProjects(), HttpStatus.OK);
            case User:
                ArrayList<Project> projs = new ArrayList<Project>(userRepo.getOne(userDetails.getForeignId()).getProjects());
                return new ResponseEntity<>(projs, HttpStatus.OK);
            case Admin:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Return the project by the given id.
     */
    @GetMapping(path = "/projects/{id}")
    public ResponseEntity<Project> getProjectById(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        switch (userDetails.getRole()) {
            case Manager:
                Project theProj = repo.getOne(id);
                if (manRepo.getOne(userDetails.getForeignId()).getProjects().contains(theProj)) {
                    return new ResponseEntity<>(theProj, HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
                theProj = repo.getOne(id);
                if (userRepo.getOne(userDetails.getForeignId()).getProjects().contains(theProj)) {
                    return new ResponseEntity<>(repo.getOne(id), HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case Admin:
            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /**
     * Update a project name or manager given by id.
     */
    @PatchMapping(path = "/projects/{id}")
    public ResponseEntity<Project> updateProject(@PathVariable long id, @RequestBody Map<String, String> params, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        if (userDetails.getRole().equals(Role.Manager)) {
            Optional<Project> pro = repo.findById(id);
            if (pro.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Project myProj = pro.get();
            if (myProj.getManager() == manRepo.getOne(userDetails.getForeignId())) {
                if (params.get("name") != null) {
                    myProj.setName(params.get("name"));
                }

                if (params.get("managerId") != null) {
                    Optional<Manager> man = manRepo.findById(Long.parseLong(params.get("managerId")));

                    if (man.isEmpty()) {
                        return new ResponseEntity<>(myProj, HttpStatus.NOT_FOUND);
                    }
                    myProj.setManager(man.get());

                }
                return new ResponseEntity<>(repo.save(myProj), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    /*
     * Update a project user list by adding or deleting the list in the body depending on the boolean add.
     */
    @PatchMapping(path = "/projects/{id}/users/{add}")
    public ResponseEntity<Project> updateProjectUsers(@PathVariable long id, @PathVariable boolean add, @RequestBody List<Long> users, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();
        if (userDetails.getRole().equals(Role.Manager)) {

            Optional<Project> pro = repo.findById(id);
            if (pro.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
            Project myProj = pro.get();

            if (myProj.getManager() == manRepo.getOne(userDetails.getForeignId())) {

                List<User> params = userRepo.findAllById(users);
                Set<User> managed = myProj.getManager().getUsers();
                Set<User> userList = myProj.getUsers();
                if (add) {
                    params.forEach(user -> {
                        if (managed.contains(user)) {
                            user.addProject(myProj);
                            userRepo.save(user);
                            userList.add(user);
                        }
                    });
                } else {
                    params.forEach(user -> {
                        if (managed.contains(user)) {
                            user.removeProject(myProj);
                            userRepo.save(user);
                            userList.remove(user);
                        }
                    });
                }
                myProj.setUsers(userList);

                return new ResponseEntity<>(repo.save(myProj), HttpStatus.OK);
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping(path = "/projects/{id}")
    public ResponseEntity<Project> deleteProject(@PathVariable long id, Authentication authentication) {
        AuthenticableUserDetails userDetails = (AuthenticableUserDetails) authentication.getPrincipal();

        switch (userDetails.getRole()) {
            case Manager:
                Project theProj = repo.getOne(id);
                if (manRepo.getOne(userDetails.getForeignId()).getProjects().contains(theProj)) {
                    for (User user : theProj.getUsers()) {
                        user.getProjects().remove(theProj);
                        userRepo.save(user);
                    }

                    repo.deleteById(id);
                    return new ResponseEntity<>(HttpStatus.OK);
                } else {
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            case User:
            case Admin:

            default:
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
    }
}
