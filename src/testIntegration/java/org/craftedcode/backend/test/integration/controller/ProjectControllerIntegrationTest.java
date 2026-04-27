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
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.ProjectStatus;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.ProjectDto;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ProjectRepository;
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
class ProjectControllerIntegrationTest {
  private static final String API_URL = "/v1/projects";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TestingUtils testingUtils;
  @Autowired private UserRepository userRepository;
  @Autowired private OrganizationRepository organizationRepository;
  @Autowired private ProjectRepository projectRepository;

  @Nested
  class WhenAdminOfOrgA {
    private Organization orgA;
    private Organization orgB;
    private User adminA;
    private String tokenAdminA;
    private Project projectA;
    private Project projectB;

    @BeforeEach
    void setUp() throws Exception {
      projectRepository.deleteAll();
      userRepository.deleteAll();
      organizationRepository.deleteAll();

      orgA = testingUtils.createOrganization("Org A", "org-a");
      orgB = testingUtils.createOrganization("Org B", "org-b");
      adminA = testingUtils.createAdminUser(orgA);
      testingUtils.createUserInOrg("adminb@example.com", testingUtils.getAdminRole(), orgB);
      tokenAdminA = testingUtils.createAccessToken(adminA, mockMvc);

      projectA =
          projectRepository.save(
              Project.builder()
                  .name("Project A")
                  .organization(orgA)
                  .status(ProjectStatus.ACTIVE)
                  .build());
      projectB =
          projectRepository.save(
              Project.builder()
                  .name("Project B")
                  .organization(orgB)
                  .status(ProjectStatus.ACTIVE)
                  .build());
    }

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
      projectRepository.deleteAll();
      userRepository.deleteAll();
      organizationRepository.deleteAll();
      testingUtils.clear();
    }

    @Test
    void findAll_returnsOnlyOwnOrgProjects() throws Exception {
      mockMvc
          .perform(get(API_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].name", is(projectA.getName())));
    }

    @Test
    void findById_ownProject_returns200() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "/{id}", projectA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", is(projectA.getName())));
    }

    @Test
    void findById_otherOrgProject_returns404() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "/{id}", projectB.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNotFound());
    }

    @Test
    void create_inOwnOrg_returns201() throws Exception {
      ProjectDto newProject = new ProjectDto(null, ProjectStatus.ACTIVE, orgA.getId());
      newProject.setName("New Project");

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(newProject)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name", is("New Project")));
    }

    @Test
    void create_inOtherOrg_returns404() throws Exception {
      ProjectDto newProject = new ProjectDto(null, ProjectStatus.ACTIVE, orgB.getId());
      newProject.setName("Hijacked Project");

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(newProject)))
          .andExpect(status().isNotFound());
    }

    @Test
    void update_ownProject_returns200() throws Exception {
      ProjectDto update = new ProjectDto(null, ProjectStatus.ACTIVE, orgA.getId());
      update.setId(projectA.getId());
      update.setName("Updated Project A");

      mockMvc
          .perform(
              put(API_URL + "/{id}", projectA.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", is("Updated Project A")));
    }

    @Test
    void update_otherOrgProject_returns404() throws Exception {
      ProjectDto update = new ProjectDto(null, ProjectStatus.ACTIVE, orgB.getId());
      update.setId(projectB.getId());
      update.setName("Hijacked Project B");

      mockMvc
          .perform(
              put(API_URL + "/{id}", projectB.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isNotFound());
    }

    @Test
    void delete_ownProject_returns204() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", projectA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNoContent());

      assertFalse(projectRepository.existsById(Objects.requireNonNull(projectA.getId())));
    }

    @Test
    void delete_otherOrgProject_returns404() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", projectB.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  class WhenViewerOfOrgA {
    private Organization orgA;
    private User viewerA;
    private String tokenViewerA;
    private Project projectA;

    @BeforeEach
    void setUp() throws Exception {
      projectRepository.deleteAll();
      userRepository.deleteAll();
      organizationRepository.deleteAll();

      orgA = testingUtils.createOrganization("Org A", "org-a");
      viewerA =
          testingUtils.createUserInOrg(
              "viewer@example.com", testingUtils.getRoleByName("VIEWER"), orgA);
      tokenViewerA = testingUtils.createAccessToken(viewerA, mockMvc);

      projectA =
          projectRepository.save(
              Project.builder()
                  .name("Project A")
                  .organization(orgA)
                  .status(ProjectStatus.ACTIVE)
                  .build());
    }

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
      projectRepository.deleteAll();
      userRepository.deleteAll();
      organizationRepository.deleteAll();
      testingUtils.clear();
    }

    @Test
    void findAll_returnsOwnOrgProjects() throws Exception {
      mockMvc
          .perform(get(API_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenViewerA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].name", is(projectA.getName())));
    }

    @Test
    void create_returns403() throws Exception {
      ProjectDto newProject = new ProjectDto(null, ProjectStatus.ACTIVE, orgA.getId());
      newProject.setName("New Project");

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenViewerA)
                  .content(objectMapper.writeValueAsString(newProject)))
          .andExpect(status().isForbidden());
    }

    @Test
    void delete_returns403() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", projectA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenViewerA))
          .andExpect(status().isForbidden());
    }
  }
}
