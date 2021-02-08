package fr.tse.poc.controllerTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.ProjectRepository;
import fr.tse.poc.dao.TimeCheckRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Project;
import fr.tse.poc.domain.TimeCheck;
import fr.tse.poc.domain.User;


@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
class TimeCheckControllerTest {
	
	
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
    
    @Autowired
    private TimeCheckRepository timeCheckRepository;
    
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testGetAllUser() throws Exception {
    	mvc.perform(get("/TimeCheck")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
	}

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testGetAllManager() throws Exception {
    	mvc.perform(get("/TimeCheck")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].id", is(1L)));	
	}
    
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testGetAllAdmin() throws Exception {
		mvc.perform(get("/TimeCheck")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
	}
	

    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
   	@Test
   	public void testGetOneUser() throws Exception {
       	mvc.perform(get("/TimeCheck/1")
                   .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk());
       	
       	mvc.perform(get("/TimeCheck/3")
                .contentType(MediaType.APPLICATION_JSON))
       			.andExpect(status().isUnauthorized());
   	}

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
   	@Test
   	public void testGetOneManager() throws Exception {
       	mvc.perform(get("/TimeCheck/1")
                   .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isOk())
                   .andExpect(jsonPath("$.[0].id", is(1L)));	
       	
       	mvc.perform(get("/TimeCheck/3")
                .contentType(MediaType.APPLICATION_JSON))
       			.andExpect(status().isUnauthorized());	
   	}
       
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
   	@Test
   	public void testGetOneAdmin() throws Exception {
   		mvc.perform(get("/TimeCheck/1")
                   .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isUnauthorized());
   	}

    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
   	@Test
   	public void testDelTimeUser() throws Exception {
       	mvc.perform(delete("/TimeCheck/2")
                   .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isUnauthorized());
       	
       	User user1 = userRepository.findById(1L).orElseThrow();
       	Project project2 = projectRepository.findById(2L).orElseThrow();
       	List<TimeCheck> timeChecks = IntStream.range(0, 2).boxed()
                .map(i -> new TimeCheck(i / 8.)).collect(Collectors.toList());

        user1.addTimeCheck(timeChecks.get(0));
        project2.addTimeCheck(timeChecks.get(0));
        timeCheckRepository.saveAll(timeChecks);

    	mvc.perform(delete("/TimeCheck/6")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    	
    	assertTrue(timeCheckRepository.findById(6L).isEmpty());
    	
    	
       	
   	}

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
   	@Test
   	public void testDelTimeManager() throws Exception {
       	mvc.perform(delete("/TimeCheck/3")
       			.contentType(MediaType.APPLICATION_JSON))
       			.andExpect(status().isUnauthorized());
   	}
       
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
   	@Test
   	public void testDelTimeAdmin() throws Exception {
   		mvc.perform(delete("/TimeCheck/1")
                   .contentType(MediaType.APPLICATION_JSON))
                   .andExpect(status().isUnauthorized());
   	}

    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testAddTimeUser() throws Exception {
    	mvc.perform(post("/TimeCheck")
    			.content(" projectId,{4} \n time, {15} ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    	
    	assertEquals( timeCheckRepository.findById(6L).get().getUser().getLastname(), "user1" );
    	
    	timeCheckRepository.deleteById(6L);
	}

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testAddTimeManager() throws Exception {
    	mvc.perform(post("/TimeCheck")
    			.content(" projectId,{4} \n time, {15} ")
                .contentType(MediaType.APPLICATION_JSON))
    	 		.andExpect(status().isUnauthorized());
	}
    
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testAddTimeAdmin() throws Exception {
		mvc.perform(post("/TimeCheck")
				.content(" projectId,{4} \n time, {15} ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
	}

	
    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testModTimeUser() throws Exception {
    	mvc.perform(patch("/TimeCheck/1")
    			.content(" { \"projectId\" : \"1\", \"time\" : \"5\" } ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    	
    	assertEquals( timeCheckRepository.findById(6L).get().getUser().getLastname(), "user1" );
    	
    	timeCheckRepository.deleteById(6L);
	}

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testModTimeManager() throws Exception {
    	mvc.perform(patch("/TimeCheck")
    			.content(" { \"projectId\" : \"1\", \"time\" : \"5\" } ")
                .contentType(MediaType.APPLICATION_JSON))
    	 		.andExpect(status().isOk());
	}
    
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void testModTimeAdmin() throws Exception {
		mvc.perform(patch("/TimeCheck")
				.content(" { \"projectId\" : \"1\", \"time\" : \"5\" } ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
	}

}
