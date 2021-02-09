package fr.tse.poc.controllerTest;


import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Manager;
import fr.tse.poc.domain.Project;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
public class ProjectControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ManagerRepository managerRepository;
    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetProjectsUser() throws Exception {
        mvc.perform(get("/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name", is("project 1 users")));
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetProjectsManager() throws Exception {
        mvc.perform(get("/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name", is("project empty")))
                .andExpect(jsonPath("$.[1].name", is("project 1 users")));

    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetProjectsAdmin() throws Exception {
        mvc.perform(get("/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetProjectByIdUser() throws Exception {
        mvc.perform(get("/projects/2")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("project 1 users")));


        mvc.perform(get("/projects/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetProjectByIdManager() throws Exception {
        mvc.perform(get("/projects/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("project empty")));

        mvc.perform(get("/projects/3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetProjectByIdAdmin() throws Exception {
        mvc.perform(get("/projects/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testDelProjectUser() throws Exception {
        mvc.perform(delete("/projects/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testDelProjectManager() throws Exception {
        Manager manager1 = managerRepository.findById(1L).orElseThrow();


        Project projectEmpty = new Project("project to delete");
        projectEmpty.setManager(manager1);
        projectRepository.save(projectEmpty);
        

        mvc.perform(delete("/projects/"+ projectEmpty.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        Assertions.assertTrue(projectRepository.findById(projectEmpty.getId()).isEmpty());

        mvc.perform(delete("/projects/3")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());


    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testDelProjectAdmin() throws Exception {
        mvc.perform(delete("/projects/1")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testAddProjectUser() throws Exception {
        mvc.perform(post("/projects")
        		.content("name , New project")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testAddProjectManager() throws Exception {
        mvc.perform(post("/projects")
                .content("name : New project")
                .contentType(MediaType.MULTIPART_FORM_DATA))
                .andExpect(status().isOk());

        Assertions.assertTrue(projectRepository.findById(4L).isPresent());
        assertEquals("New project", projectRepository.findById(4L).get().getName());

        projectRepository.deleteById(4L);

    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testAddProjectAdmin() throws Exception {
        mvc.perform(post("/projects")
        		.content("name , New project")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModProjectBaseUser() throws Exception {
        mvc.perform(patch("/projects/1")
        		.content("{ \"name\" : \"new Name\" }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModProjectBaseManager() throws Exception {
        mvc.perform(patch("/projects/1")
                .content("{ \"name\" : \"new Name\" }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("new Name")));
        projectRepository.findById(1L).get().setName("project empty");

        mvc.perform(patch("/projects/3")
        		.content("{ \"name\" : \"new Name\" }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());


    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModProjectBaseAdmin() throws Exception {
        mvc.perform(patch("/projects/1")
        		.content("{ \"name\" : \"new Name\" }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModProjectUsersUser() throws Exception {
        mvc.perform(patch("/projects/1/users")
        		.content("users, {[2]} \n add, True ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModProjectUsersManager() throws Exception {
    	
    	
        mvc.perform(patch("/projects/1/users")
                .content("{ \"users\" :  {[\"2\"]} , \"add\" :  \"True\"  }")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name", is("New Name")));
        projectRepository.findById(1L).get().setName("project empty");

        mvc.perform(patch("/projects/3/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());


    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModProjectUsersAdmin() throws Exception {
        mvc.perform(patch("/projects/1/users")
        		 .content("users, {[2]} \n add, True ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }


}
