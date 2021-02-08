package fr.tse.poc.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.User;
import fr.tse.poc.service.UserService;
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
public class UserControllerTest {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private ManagerRepository managerRepository;
    @Autowired
    private AdminRepository adminRepository;
    @Autowired
    private UserService userService;

    public static String asJsonString(final Object obj) {
        try {
            return new ObjectMapper().writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
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

    @WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getUserByIdTest1() throws Exception {
        Long user1Id = userRepository.findAll().get(0).getId();
        mvc.perform(get("/users/" + user1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Jean")))
                .andExpect(jsonPath("$.lastname", is("Bon")));
    }

    @WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getUserByIdTest2() throws Exception {
        // user managed by authentified manager
        Long user1Id = userRepository.findAll().get(0).getId();
        mvc.perform(get("/users/" + user1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("Jean")))
                .andExpect(jsonPath("$.lastname", is("Bon")));

        // other user
        Long user2Id = userRepository.findAll().get(1).getId();
        mvc.perform(get("/users/" + user2Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());

    }

    @WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void postUserTest() throws Exception {

        User user = new User("John", "Doe");
        int size = this.userRepository.findAll().size();
        mvc.perform(post("/users")
                .content(asJsonString(user))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(size + 1, this.userRepository.findAll().size());
    }

    @WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void deleteUserTest() throws Exception {
        int size = this.userRepository.findAll().size();

        Long user1Id = userRepository.findAll().get(0).getId();
        mvc.perform(delete("/users/" + user1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(size - 1, this.userRepository.findAll().size());

    }

    @WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void patchUserTest1() throws Exception {
        Long user1Id = userRepository.findAll().get(1).getId();
        User user1 = userRepository.getOne(user1Id);

        Long id = userService.getManagerId(user1);
        assertEquals(null, id);

        Long managerId = managerRepository.findAll().get(0).getId();

        mvc.perform(patch("/users/" + user1Id)
                .content("{\"manager\":" + managerId + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manager.firstname", is("Jeremy")));


    }

    @WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void patchUserTest2() throws Exception {
        Long user1Id = userRepository.findAll().get(1).getId();
        int size = this.adminRepository.findAll().size();

        mvc.perform(patch("/users/" + user1Id)
                .content("{\"status\":\"Admin\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertEquals(size + 1, this.adminRepository.findAll().size());
    }
}