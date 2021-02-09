package fr.tse.poc.controllerTest;

import fr.tse.poc.dao.*;
import fr.tse.poc.domain.Project;
import fr.tse.poc.domain.TimeCheck;
import fr.tse.poc.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


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
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(4)));
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetAllManager() throws Exception {
        mvc.perform(get("/TimeCheck")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id", is(4)));
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
        mvc.perform(get("/TimeCheck/4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        mvc.perform(get("/TimeCheck/6")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetOneManager() throws Exception {
        mvc.perform(get("/TimeCheck/4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(4)));

        mvc.perform(get("/TimeCheck/6")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testGetOneAdmin() throws Exception {
        mvc.perform(get("/TimeCheck/4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testDelTimeUser() throws Exception {
        mvc.perform(delete("/TimeCheck/5")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

        User user1 = userRepository.findById(1L).orElseThrow();
        Project project2 = projectRepository.findById(2L).orElseThrow();
        List<TimeCheck> timeChecks = IntStream.range(0, 1).boxed()
                .map(i -> new TimeCheck(i / 8.)).collect(Collectors.toList());

        user1.addTimeCheck(timeChecks.get(0));
        project2.addTimeCheck(timeChecks.get(0));
        timeCheckRepository.saveAll(timeChecks);

        mvc.perform(delete("/TimeCheck/10")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertTrue(timeCheckRepository.findById(10L).isEmpty());


    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testDelTimeManager() throws Exception {
        mvc.perform(delete("/TimeCheck/6")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testDelTimeAdmin() throws Exception {
        mvc.perform(delete("/TimeCheck/4")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testAddTimeUser() throws Exception {
        
    
    	
    	mvc.perform(post("/TimeCheck")
        		 .queryParam("projectId", "4")
        		 .queryParam("time", "15"))
                .andExpect(status().isOk());

        assertEquals(timeCheckRepository.findById(10L).get().getUser().getLastname(), "user1");

        timeCheckRepository.deleteById(10L);
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testAddTimeManager() throws Exception {
        mvc.perform(post("/TimeCheck")
                .content(" projectId,{4} \n time, {15} "))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testAddTimeAdmin() throws Exception {
        mvc.perform(post("/TimeCheck")
                .content(" projectId,{4} \n time, {15} "))
                .andExpect(status().isUnauthorized());
    }


    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModTimeUser() throws Exception {
        mvc.perform(patch("/TimeCheck/4")
                .content(" { \"projectId\" : \"1\", \"time\" : \"5\" } ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(timeCheckRepository.findById(4L).get().getTime(), 5.0);
        
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModTimeManager() throws Exception {
        mvc.perform(patch("/TimeCheck/4")
                .content(" { \"projectId\" : \"1\", \"time\" : \"5\" } ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void testModTimeAdmin() throws Exception {
        mvc.perform(patch("/TimeCheck/4")
                .content(" { \"projectId\" : \"1\", \"time\" : \"5\" } ")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

}
