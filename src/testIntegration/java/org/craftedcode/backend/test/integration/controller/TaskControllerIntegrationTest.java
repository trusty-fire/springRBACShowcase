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
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.TaskPriority;
import org.craftedcode.backend.model.TaskStatus;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.TaskDto;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.TaskRepository;
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
class TaskControllerIntegrationTest {
  private static final String API_URL = "/v1/tasks";

  @Autowired private MockMvc mockMvc;
  @Autowired private ObjectMapper objectMapper;
  @Autowired private TestingUtils testingUtils;
  @Autowired private UserRepository userRepository;
  @Autowired private OrganizationRepository organizationRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private TaskRepository taskRepository;

  @Nested
  class WhenAdminOfOrgA {
    private Organization orgA;
    private Organization orgB;
    private User adminA;
    private String tokenAdminA;
    private Project projectA;
    private Project projectB;
    private Task taskA;
    private Task taskB;

    @BeforeEach
    void setUp() throws Exception {
      taskRepository.deleteAll();
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

      taskA =
          taskRepository.save(
              Task.builder()
                  .name("Task A")
                  .project(projectA)
                  .status(TaskStatus.TODO)
                  .priority(TaskPriority.MEDIUM)
                  .build());
      taskB =
          taskRepository.save(
              Task.builder()
                  .name("Task B")
                  .project(projectB)
                  .status(TaskStatus.TODO)
                  .priority(TaskPriority.MEDIUM)
                  .build());
    }

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
      taskRepository.deleteAll();
      projectRepository.deleteAll();
      userRepository.deleteAll();
      organizationRepository.deleteAll();
      testingUtils.clear();
    }

    @Test
    void findAll_returnsOnlyOwnOrgTasks() throws Exception {
      mockMvc
          .perform(get(API_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].name", is(taskA.getName())));
    }

    @Test
    void findById_ownTask_returns200() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "/{id}", taskA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", is(taskA.getName())));
    }

    @Test
    void findById_otherOrgTask_returns404() throws Exception {
      mockMvc
          .perform(
              get(API_URL + "/{id}", taskB.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNotFound());
    }

    @Test
    void create_inOwnOrgProject_returns201() throws Exception {
      TaskDto newTask =
          new TaskDto(null, TaskStatus.TODO, TaskPriority.LOW, null, projectA.getId());
      newTask.setName("New Task");

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(newTask)))
          .andExpect(status().isCreated())
          .andExpect(jsonPath("$.name", is("New Task")));
    }

    @Test
    void create_inOtherOrgProject_returns404() throws Exception {
      TaskDto newTask =
          new TaskDto(null, TaskStatus.TODO, TaskPriority.LOW, null, projectB.getId());
      newTask.setName("Hijacked Task");

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(newTask)))
          .andExpect(status().isNotFound());
    }

    @Test
    void update_ownTask_returns200() throws Exception {
      TaskDto update =
          new TaskDto(null, TaskStatus.IN_PROGRESS, TaskPriority.HIGH, null, projectA.getId());
      update.setId(taskA.getId());
      update.setName("Updated Task A");

      mockMvc
          .perform(
              put(API_URL + "/{id}", taskA.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.name", is("Updated Task A")));
    }

    @Test
    void update_otherOrgTask_returns404() throws Exception {
      TaskDto update = new TaskDto(null, TaskStatus.TODO, TaskPriority.LOW, null, projectB.getId());
      update.setId(taskB.getId());
      update.setName("Hijacked Task B");

      mockMvc
          .perform(
              put(API_URL + "/{id}", taskB.getId())
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA)
                  .content(objectMapper.writeValueAsString(update)))
          .andExpect(status().isNotFound());
    }

    @Test
    void delete_ownTask_returns204() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", taskA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenAdminA))
          .andExpect(status().isNoContent());

      assertFalse(taskRepository.existsById(Objects.requireNonNull(taskA.getId())));
    }

    @Test
    void delete_otherOrgTask_returns404() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", taskB.getId())
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
    private Task taskA;

    @BeforeEach
    void setUp() throws Exception {
      taskRepository.deleteAll();
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
      taskA =
          taskRepository.save(
              Task.builder()
                  .name("Task A")
                  .project(projectA)
                  .status(TaskStatus.TODO)
                  .priority(TaskPriority.MEDIUM)
                  .build());
    }

    @AfterEach
    void tearDown() {
      SecurityContextHolder.clearContext();
      taskRepository.deleteAll();
      projectRepository.deleteAll();
      userRepository.deleteAll();
      organizationRepository.deleteAll();
      testingUtils.clear();
    }

    @Test
    void findAll_returnsOwnOrgTasks() throws Exception {
      mockMvc
          .perform(get(API_URL).header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenViewerA))
          .andExpect(status().isOk())
          .andExpect(jsonPath("$.content", hasSize(1)))
          .andExpect(jsonPath("$.content[0].name", is(taskA.getName())));
    }

    @Test
    void create_returns403() throws Exception {
      TaskDto newTask =
          new TaskDto(null, TaskStatus.TODO, TaskPriority.LOW, null, projectA.getId());
      newTask.setName("New Task");

      mockMvc
          .perform(
              post(API_URL)
                  .contentType(MediaType.APPLICATION_JSON)
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenViewerA)
                  .content(objectMapper.writeValueAsString(newTask)))
          .andExpect(status().isForbidden());
    }

    @Test
    void delete_returns403() throws Exception {
      mockMvc
          .perform(
              delete(API_URL + "/{id}", taskA.getId())
                  .header(HttpHeaders.AUTHORIZATION, "Bearer " + tokenViewerA))
          .andExpect(status().isForbidden());
    }
  }
}
