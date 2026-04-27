package org.craftedcode.backend.test.integration.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.frachtwerk.essencium.backend.model.dto.BaseUserDto;
import jakarta.servlet.ServletContext;
import java.io.Serializable;
import java.util.Objects;
import java.util.Set;
import org.craftedcode.backend.controller.UserController;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.AppUserDto;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.UserRepository;
import org.craftedcode.backend.test.integration.IntegrationTestApplication;
import org.craftedcode.backend.test.integration.util.TestingUtils;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockServletContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.context.WebApplicationContext;

@SpringBootTest(
    classes = IntegrationTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerIntegrationTest {
  private static final String API_URL = "/v1/users";

  @Autowired private WebApplicationContext webApplicationContext;
  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TestingUtils testingUtils;
  @Autowired private UserRepository userRepository;
  @Autowired private OrganizationRepository organizationRepository;

  private User executingUser;
  private String accessToken;

  private User userA;
  private User userB;

  @Test
  void checkUserControllerExistence() {
    ServletContext servletContext = webApplicationContext.getServletContext();

    assertNotNull(servletContext);
    assertInstanceOf(MockServletContext.class, servletContext);
    assertNotNull(webApplicationContext.getBean(UserController.class));
  }

  @Nested
  class TestAdminAccess {
    private Organization org;

    @BeforeEach
    void setUp() throws Exception {
      userRepository.deleteAll();
      organizationRepository.deleteAll();

      org = testingUtils.createOrganization("Test Org", "test-org");
      executingUser = testingUtils.createAdminUser(org);
      accessToken = testingUtils.createAccessToken(executingUser, mockMvc);

      userA = testingUtils.createUserInOrg("userA@example.com", testingUtils.getAdminRole(), org);
      userB = testingUtils.createUserInOrg("userB@example.com", testingUtils.getAdminRole(), org);
    }

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
      userRepository.deleteAll();
      organizationRepository.deleteAll();
      testingUtils.clear();
    }

    @Test
    void findAllUsers() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "?sort=id,asc")
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(3)))
          .andExpect(jsonPath("$.content[0].email", is(executingUser.getEmail())))
          .andExpect(jsonPath("$.content[1].email", is(userA.getEmail())))
          .andExpect(jsonPath("$.content[2].email", is(userB.getEmail())));
    }

    @Test
    void findUserById() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "/{id}", userA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", is(userA.getEmail())));
    }

    @Test
    void createUser() throws Exception {
      BaseUserDto<Serializable> newUserA =
          AppUserDto.builder()
              .email("newusera@example.com")
              .firstName("first")
              .lastName("last")
              .roles(Set.of())
              .build();

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                  .content(objectMapper.writeValueAsString(newUserA)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email", is(newUserA.getEmail())));

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                  .content(objectMapper.writeValueAsString(newUserA)))
          .andExpect(status().isConflict());

      BaseUserDto<Serializable> newUserB =
          AppUserDto.builder()
              .email("newuserb@example.com")
              .firstName("first")
              .lastName("last")
              .roles(Set.of())
              .build();

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                  .content(objectMapper.writeValueAsString(newUserB)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email", is(newUserB.getEmail())));
    }

    @Test
    void updateUser() throws Exception {
      BaseUserDto<Serializable> updatedUserA =
          AppUserDto.builder()
              .id(userA.getId())
              .email("updateduseraemail@example.com")
              .firstName("update first name")
              .lastName(userA.getLastName())
              .roles(Set.of())
              .build();

      mockMvc
          .perform(
              put(API_URL + "/{id}", updatedUserA.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                  .content(objectMapper.writeValueAsString(updatedUserA)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", is(updatedUserA.getEmail())))
          .andExpect(jsonPath("$.firstName", is(updatedUserA.getFirstName())));

      mockMvc
          .perform(
              put(API_URL + "/{id}", updatedUserA.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                  .content(objectMapper.writeValueAsString(updatedUserA)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.email", is(updatedUserA.getEmail())));
    }

    @Test
    void deleteUser() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", userA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken))
          .andExpect(status().isNoContent());

      assertFalse(userRepository.existsById(Objects.requireNonNull(userA.getId())));
    }
  }
}
