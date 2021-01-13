package fr.tse.poc.controllerTest;

import static org.junit.Assert.assertEquals;

import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import fr.tse.poc.dao.UserRepository;

@RunWith(SpringRunner.class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
public class UserControllerTest {
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    
    
    @WithUserDetails("a")
    @Test
    public void getAllUserTest() throws Exception {
    	assertEquals(1,userRepository.findAll().size());
    	assertEquals("Jean",userRepository.findAll().get(0).getFirstname());
    	
//    	mvc.perform(get("/users").contentType(MediaType.APPLICATION_JSON))
//          .andExpect(status().isOk())
//          .andExpect(jsonPath("$.[0].firstname", is("Jean")));
    }
    
}
