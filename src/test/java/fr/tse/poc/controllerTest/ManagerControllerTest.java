package fr.tse.poc.controllerTest;

import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.domain.Manager;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles(profiles = "test")
@AutoConfigureMockMvc
public class ManagerControllerTest {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private ManagerRepository managerRepository;

	@WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getAllManagerTest() throws Exception {
		mvc.perform(get("/managers").contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$", hasSize(2)))
				.andExpect(jsonPath("$.[0].firstname", is("manager1")));
	}

	@WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getAllManagerTest2() throws Exception {
		mvc.perform(get("/managers").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}

	@WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getAllManagerTest3() throws Exception {
		mvc.perform(get("/managers").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}

	@WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getManagerByIdTest1() throws Exception {

		Long id = managerRepository.findAll().get(0).getId();

		mvc.perform(get("/managers/" + id).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.firstname", is("manager1")));
	}

	@WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getManagerByIdTest2() throws Exception {

		Long id = managerRepository.findAll().get(0).getId();

		mvc.perform(get("/managers/" + id).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.firstname", is("manager1")));
	}

	@WithUserDetails(value = "user1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getManagerByIdTest3() throws Exception {

		Long id = managerRepository.findAll().get(0).getId();

		mvc.perform(get("/managers/" + id).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void postManagerTest() throws Exception {

		Manager manager = new Manager("John", "Doe");
		int size = this.managerRepository.findAll().size();
		mvc.perform(post("/managers").content(Utils.asJsonString(manager)).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

		assertEquals(size + 1, this.managerRepository.findAll().size());
	}

	@WithUserDetails(value = "manager1", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void postManagerTest2() throws Exception {

		Manager manager = new Manager("John", "Doe");
		mvc.perform(post("/managers").content(Utils.asJsonString(manager)).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

	}

	@WithUserDetails(value = "admin", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void deleteUserTest() throws Exception {
		Manager manager = managerRepository.save(new Manager("John", "Doe"));

		int size = this.managerRepository.findAll().size();

		mvc.perform(delete("/managers/" + manager.getId())
				.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());

		assertEquals(size - 1, this.managerRepository.findAll().size());
	}
}
