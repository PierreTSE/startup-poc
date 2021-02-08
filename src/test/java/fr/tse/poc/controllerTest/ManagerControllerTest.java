package fr.tse.poc.controllerTest;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.tse.poc.dao.ManagerRepository;
import fr.tse.poc.domain.Manager;
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
public class ManagerControllerTest {

	@Autowired
	private MockMvc mvc;
	@Autowired
	private ManagerRepository managerRepository;

	public static String asJsonString(final Object obj) {
		try {
			return new ObjectMapper().writeValueAsString(obj);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getAllManagerTest() throws Exception {
		mvc.perform(get("/managers").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.[0].firstname", is("Jeremy")));
	}

	@WithUserDetails(value = "user", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getAllManagerTest2() throws Exception {
		mvc.perform(get("/managers").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}

	@WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getAllManagerTest3() throws Exception {
		mvc.perform(get("/managers").contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());
	}

	@WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getManagerByIdTest1() throws Exception {

		Long id = managerRepository.findAll().get(0).getId();

		mvc.perform(get("/managers/" + id).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.firstname", is("Jeremy")));
	}

	@WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getManagerByIdTest2() throws Exception {

		Long id = managerRepository.findAll().get(0).getId();

		mvc.perform(get("/managers/" + id).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk())
				.andExpect(jsonPath("$.firstname", is("Jeremy")));
	}

	@WithUserDetails(value = "user", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void getManagerByIdTest3() throws Exception {

		Long id = managerRepository.findAll().get(0).getId();

		mvc.perform(get("/managers/" + id).contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isUnauthorized());
	}

	@WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void postManagerTest() throws Exception {

		Manager manager = new Manager("John", "Doe");
		int size = this.managerRepository.findAll().size();
		mvc.perform(post("/managers").content(asJsonString(manager)).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isCreated());

		assertEquals(size + 1, this.managerRepository.findAll().size());
	}

	@WithUserDetails(value = "manager", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void postManagerTest2() throws Exception {

		Manager manager = new Manager("John", "Doe");
		mvc.perform(post("/managers").content(asJsonString(manager)).accept(MediaType.APPLICATION_JSON)
				.contentType(MediaType.APPLICATION_JSON)).andExpect(status().isUnauthorized());

	}

	@WithUserDetails(value = "a", userDetailsServiceBeanName = "authenticableUserDetailsService")
	@Test
	public void deleteUserTest() throws Exception {
		int size = this.managerRepository.findAll().size();

		Long id = managerRepository.findAll().get(1).getId();
		mvc.perform(delete("/managers/" + id).contentType(MediaType.APPLICATION_JSON)).andExpect(status().isOk());

		assertEquals(size - 1, this.managerRepository.findAll().size());

	}

}
