package org.craftedcode.backend.test.integration.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Objects;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.OrganizationDto;
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
class OrganizationControllerIntegrationTest {
  private static final String API_URL = "/v1/organizations";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TestingUtils testingUtils;
  @Autowired private UserRepository userRepository;
  @Autowired private OrganizationRepository organizationRepository;

  @Nested
  class WhenAdminWithOrg {
    private Organization orgA;
    private Organization orgB;
    private User adminA;
    private String tokenAdminA;

    @BeforeEach
    void setUp() throws Exception {
      userRepository.deleteAll();
      organizationRepository.deleteAll();

      orgA = testingUtils.createOrganization("Org A", "org-a");
      orgB = testingUtils.createOrganization("Org B", "org-b");
      adminA = testingUtils.createAdminUser(orgA);
      tokenAdminA = testingUtils.createAccessToken(adminA, mockMvc);
    }

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
      userRepository.deleteAll();
      organizationRepository.deleteAll();
      testingUtils.clear();
    }

    @Test
    void findAll_returnsOnlyOwnOrg() throws Exception {
      mockMvc
          .perform(get(API_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].slug", is(orgA.getSlug())));
    }

    @Test
    void findById_ownOrg_returns200() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "/{id}", orgA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.slug", is(orgA.getSlug())));
    }

    @Test
    void findById_otherOrg_returns404() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "/{id}", orgB.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNotFound());
    }

    @Test
    void update_ownOrg_returns200() throws Exception {
      OrganizationDto update = new OrganizationDto("updated-org-a");
      update.setId(orgA.getId());
      update.setName("Updated Org A");

      mockMvc
          .perform(
              put(API_URL + "/{id}", orgA.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", is("Updated Org A")));
    }

    @Test
    void update_otherOrg_returns404() throws Exception {
      OrganizationDto update = new OrganizationDto("hijacked-org-b");
      update.setId(orgB.getId());
      update.setName("Hijacked Org B");

      mockMvc
          .perform(
              put(API_URL + "/{id}", orgB.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isNotFound());
    }

    @Test
    void delete_ownOrg_returns204() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", orgA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNoContent());

      assertFalse(organizationRepository.existsById(Objects.requireNonNull(orgA.getId())));
    }

    @Test
    void delete_otherOrg_returns404() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", orgB.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNotFound());
    }
  }
}
