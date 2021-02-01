package fr.tse.poc.controllerTest;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;

import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.User;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getAllUserTest() throws Exception {

        mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.[0].firstname", is("Jean")))
                .andExpect(jsonPath("$.[0].lastname", is("Bon")));
    }
    
    @WithUserDetails(value = "user", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getAllUserTest2() throws Exception {

        mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
    
    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getUserByIdTest() throws Exception {
    	Long user1Id=userRepository.findAll().get(0).getId();
    	mvc.perform(get("/users/"+user1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Jean")))
                .andExpect(jsonPath("$.lastname", is("Bon")));
    }
    
    @WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getUserByIdTest2() throws Exception {
    	// user managed by authentified manager 
    	Long user1Id=userRepository.findAll().get(0).getId();
    	mvc.perform(get("/users/"+user1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Jean")))
                .andExpect(jsonPath("$.lastname", is("Bon")));
    	
    	// other user
    	Long user2Id=userRepository.findAll().get(1).getId();
    	mvc.perform(get("/users/"+user2Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    	
    }
    
    @WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void postUserTest() throws Exception {
    	
    	Map<String,String> entry=new HashMap<>();
    	entry.put("firstname", "John");
    	User user=new User("John","Doe");
    	int size=this.userRepository.findAll().size();
		mvc.perform(post("/users")
				.content(asJsonString(user))
				.accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON))
		.andExpect(status().isCreated());

		assertEquals(size+1,this.userRepository.findAll().size());
    }
  
    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
}