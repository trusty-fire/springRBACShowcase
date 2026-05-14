package org.craftedcode.backend.test.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.ProjectStatus;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.TaskRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

/**
 * Boots the application with the {@code demo} profile so {@link
 * org.craftedcode.backend.configuration.initialization.DemoDataInitializer} runs on startup, then
 * verifies the seeded dataset is consistent. Uses a dedicated in-memory database so the seeded rows
 * do not leak into the other integration tests, which share a context on a different profile set.
 */
@SpringBootTest(
    classes = IntegrationTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ActiveProfiles({"development", "h2-test", "demo"})
@TestPropertySource(properties = "spring.datasource.url=jdbc:h2:mem:demo-seed-test")
class DemoDataInitializerIntegrationTest {

  @Autowired private OrganizationRepository organizationRepository;
  @Autowired private ProjectRepository projectRepository;
  @Autowired private TaskRepository taskRepository;

  @Test
  void seedsTwoTenantsWithProjectsAndTasks() {
    assertThat(organizationRepository.count()).isEqualTo(2);
    assertThat(projectRepository.count()).isEqualTo(6);
    assertThat(taskRepository.count()).isEqualTo(14);

    assertThat(organizationRepository.findAll())
        .extracting(Organization::getSlug)
        .containsExactlyInAnyOrder("acme", "globex");
  }

  @Test
  void seedsProjectsAndTasksScopedToTheirOrganization() {
    Organization acme =
        organizationRepository.findAll().stream()
            .filter(o -> "acme".equals(o.getSlug()))
            .findFirst()
            .orElseThrow();

    List<Project> acmeProjects =
        projectRepository.findAll().stream()
            .filter(p -> p.getOrganization().getId().equals(acme.getId()))
            .toList();

    assertThat(acmeProjects)
        .extracting(Project::getName)
        .containsExactlyInAnyOrder("Website Relaunch", "Mobile App", "Legacy Migration");
    assertThat(acmeProjects)
        .filteredOn(p -> p.getStatus() == ProjectStatus.ARCHIVED)
        .extracting(Project::getName)
        .containsExactly("Legacy Migration");

    long acmeTaskCount =
        taskRepository.findAll().stream()
            .filter(t -> t.getProject().getOrganization().getId().equals(acme.getId()))
            .count();
    assertThat(acmeTaskCount).isEqualTo(7);
  }
}
