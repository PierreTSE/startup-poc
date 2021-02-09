package fr.tse.poc.controllerTest;

import fr.tse.poc.dao.AdminRepository;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.dao.UserRepository;
import fr.tse.poc.domain.Manager;
import fr.tse.poc.domain.User;
import fr.tse.poc.utils.Utils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
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

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getAllUserTest() throws Exception {
        System.out.println(userRepository.findAll());

        mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$.[0].firstname", is("user1")))
                .andExpect(jsonPath("$.[0].lastname", is("user1-lastname")));
    }

    @WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getAllUserTest2() throws Exception {
        mvc.perform(get("/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getUserByIdTest1() throws Exception {
        Long user1Id = userRepository.findAll().get(0).getId();
        mvc.perform(get("/users/" + user1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("user1")))
                .andExpect(jsonPath("$.lastname", is("user1-lastname")));
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void getUserByIdTest2() throws Exception {
        // user managed by authentified manager
        Long user1Id = userRepository.findAll().get(0).getId();
        mvc.perform(get("/users/" + user1Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstname", is("user1")))
                .andExpect(jsonPath("$.lastname", is("user1-lastname")));

        // other user
        Long user2Id = userRepository.findAll().get(1).getId();
        mvc.perform(get("/users/" + user2Id)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void postUserTest() throws Exception {
        User user = new User("John", "postUserTest");
        int size = this.userRepository.findAll().size();
        mvc.perform(post("/users")
                .content(Utils.asJsonString(Map.of("user", user, "password", "a")))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());

        assertEquals(size + 1, this.userRepository.findAll().size());
        userRepository.deleteById(4L);
    }

    @WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void deleteUserTest() throws Exception {
        User user = userRepository.save(new User("John", "deleteUserTest", managerRepository.findById(1L).orElseThrow()));
        int size = this.userRepository.findAll().size();

        mvc.perform(delete("/users/" + user.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());

        assertEquals(size - 1, this.userRepository.findAll().size());
    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void patchUserTest1() throws Exception {
        User user1 = userRepository.findById(1L).orElseThrow();
        Manager oldManager = managerRepository.findById(1L).orElseThrow();
        assertEquals(user1.getManager(), oldManager);

        Manager newManager = managerRepository.findById(2L).orElseThrow();

        mvc.perform(patch("/users/" + user1.getId())
                .content("{\"manager\":" + newManager.getId() + "}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.manager.firstname", is("manager2")));
        user1 = userRepository.findById(1L).orElseThrow();
        assertEquals(newManager, user1.getManager());
        user1.setManager(oldManager);
    }

    @WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
    @Test
    public void patchUserTest2() throws Exception {
        User user = userRepository.save(new User("John", "patchUserTest2", managerRepository.getOne(1L)));

        int sizeAdmins = adminRepository.findAll().size();
        int sizeUsers = userRepository.findAll().size();

        mvc.perform(patch("/users/" + user.getId())
                .content("{\"status\":\"Admin\"}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());

        assertEquals(sizeUsers - 1, userRepository.findAll().size());
        assertEquals(sizeAdmins + 1, adminRepository.findAll().size());
        adminRepository.deleteById(2L);
    }
}