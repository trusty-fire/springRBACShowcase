package org.craftedcode.backend.test.integration.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import java.util.Set;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.AppUserDto;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.UserRepository;
import org.craftedcode.backend.test.integration.IntegrationTestApplication;
import org.craftedcode.backend.test.integration.util.TestingUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(
    classes = IntegrationTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class UserControllerTenantTest {
  private static final String API_URL = "/v1/users";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TestingUtils testingUtils;
  @Autowired private UserRepository userRepository;
  @Autowired private OrganizationRepository organizationRepository;

  @Nested
  class WhenAdminOfOrgA {
    private Organization orgA;
    private Organization orgB;
    private User adminA;
    private String tokenAdminA;
    private User userInOrgA;
    private User userInOrgB;

    @BeforeEach
    void setUp() throws Exception {
      userRepository.deleteAll();
      organizationRepository.deleteAll();

      orgA = testingUtils.createOrganization("Org A", "org-a");
      orgB = testingUtils.createOrganization("Org B", "org-b");
      adminA = testingUtils.createAdminUser(orgA);
      tokenAdminA = testingUtils.createAccessToken(adminA, mockMvc);

      userInOrgA =
          testingUtils.createUserInOrg("member-a@example.com", testingUtils.getAdminRole(), orgA);
      userInOrgB =
          testingUtils.createUserInOrg("member-b@example.com", testingUtils.getAdminRole(), orgB);
    }

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
      userRepository.deleteAll();
      organizationRepository.deleteAll();
      testingUtils.clear();
    }

    @Test
    void findAll_returnsOnlyUsersInSameOrg() throws Exception {
      mockMvc
          .perform(get(API_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void findById_userInOtherOrg_returns404() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "/{id}", userInOrgB.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNotFound());
    }

    @Test
    void create_forcesOwnOrg() throws Exception {
      AppUserDto newUser = new AppUserDto();
      newUser.setEmail("new-user@example.com");
      newUser.setFirstName("New");
      newUser.setLastName("User");
      newUser.setEnabled(true);
      newUser.setOrganizationId(orgB.getId());
      newUser.setRoles(Set.of());

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(newUser)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.email").value("new-user@example.com"));

      assertTrue(
          userRepository
              .findByEmailIgnoreCase("new-user@example.com")
              .map(u -> Objects.equals(orgA.getId(), u.getOrganization().getId()))
              .orElse(false),
          "New user must be placed in caller's org, not the requested org");
    }

    @Test
    void update_userInOtherOrg_returns404() throws Exception {
      AppUserDto update = new AppUserDto();
      update.setId(userInOrgB.getId());
      update.setEmail(userInOrgB.getEmail());
      update.setFirstName("Hijacked");
      update.setLastName("User");
      update.setEnabled(true);
      update.setRoles(Set.of());

      mockMvc
          .perform(
              put(API_URL + "/{id}", userInOrgB.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isNotFound());
    }

    @Test
    void delete_userInOtherOrg_returns404() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", userInOrgB.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNotFound());
    }
  }
}
