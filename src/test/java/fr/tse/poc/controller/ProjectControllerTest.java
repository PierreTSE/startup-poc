package fr.tse.poc.controller;

import static org.junit.Assert.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagedRepository;
import fr.tse.poc.dao.ManagerRepository;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
public class ProjectControllerTest {

	@Autowired
    private MockMvc mvc;
    @Autowired
    private ManagedRepository userRepository;
    @Autowired
    private ManagerRepository managerRepository;
    @Autowired
    private AdminRepository adminRepository;
    
    @WithUserDetails(value = "user", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testGetProjectsUser() throws Exception {
    	mvc.perform(get("/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name", is("Death Star")));
	}

    @WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testGetProjectsManager() throws Exception {
    	mvc.perform(get("/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].name", is("Death Star")))
                .andExpect(jsonPath("$.[1].name", is("Death Star The Return")));
		
	}
    
    @WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testGetProjectsAdmin() throws Exception {
		mvc.perform(get("/projects")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
	}
	@Test
	public void testGetProjectById() {
		fail("Not yet implemented");
	}

	@Test
	public void testDelProject() {
		fail("Not yet implemented");
	}

	@Test
	public void testAddProject() {
		fail("Not yet implemented");
	}

	@Test
	public void testModProjectBase() {
		fail("Not yet implemented");
	}

	@Test
	public void testModProjectUsers() {
		fail("Not yet implemented");
	}

}
