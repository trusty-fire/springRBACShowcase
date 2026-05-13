package org.craftedcode.backend.configuration.initialization;

import de.frachtwerk.essencium.backend.model.Role;
import de.frachtwerk.essencium.backend.repository.RoleRepository;
import java.time.LocalDate;
import java.util.Set;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.ProjectStatus;
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.TaskPriority;
import org.craftedcode.backend.model.TaskStatus;
import org.craftedcode.backend.model.dto.AppUserDto;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.TaskRepository;
import org.craftedcode.backend.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

@Component
@Profile("demo")
public class DemoDataInitializer {

  private static final Logger log = LoggerFactory.getLogger(DemoDataInitializer.class);
  static final String DEMO_PASSWORD = "demo123";

  private final OrganizationRepository organizationRepository;
  private final ProjectRepository projectRepository;
  private final TaskRepository taskRepository;
  private final UserService userService;
  private final RoleRepository roleRepository;

  public DemoDataInitializer(
      OrganizationRepository organizationRepository,
      ProjectRepository projectRepository,
      TaskRepository taskRepository,
      UserService userService,
      RoleRepository roleRepository) {
    this.organizationRepository = organizationRepository;
    this.projectRepository = projectRepository;
    this.taskRepository = taskRepository;
    this.userService = userService;
    this.roleRepository = roleRepository;
  }

  // Runs after essencium's DataInitializationService (@Order(100000)) so roles/rights exist.
  @EventListener(ApplicationReadyEvent.class)
  @Order(200_000)
  public void seed() {
    if (organizationRepository.count() > 0) {
      log.info("Demo data: organizations already present, skipping seeding.");
      return;
    }
    log.info("Demo data: seeding two organizations with users, projects and tasks.");

    Organization acme = createOrg("Acme Inc", "acme");
    Organization globex = createOrg("Globex Corp", "globex");

    seedTenant(acme, "acme");
    seedTenant(globex, "globex");

    log.info("Demo data: seeded. Sample login → admin@acme.crafted-code.org / {}", DEMO_PASSWORD);
  }

  private void seedTenant(Organization org, String slug) {
    createUser("admin@" + slug + ".crafted-code.org", "Alice", "Admin", org, "ADMIN");
    createUser("manager@" + slug + ".crafted-code.org", "Marvin", "Manager", org, "MANAGER");
    createUser("member@" + slug + ".crafted-code.org", "Mia", "Member", org, "MEMBER");
    createUser("viewer@" + slug + ".crafted-code.org", "Victor", "Viewer", org, "VIEWER");

    Project website =
        createProject(org, "Website Relaunch", "Marketing site redesign", ProjectStatus.ACTIVE);
    Project mobile =
        createProject(org, "Mobile App", "Native iOS + Android client", ProjectStatus.ACTIVE);
    Project legacy =
        createProject(
            org, "Legacy Migration", "Move billing off the monolith", ProjectStatus.ARCHIVED);

    createTask(website, "Draft hero section", TaskStatus.DONE, TaskPriority.MEDIUM, -7);
    createTask(website, "Wire up CMS", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 3);
    createTask(website, "Cookie banner copy", TaskStatus.TODO, TaskPriority.LOW, 14);

    createTask(mobile, "Push notification spike", TaskStatus.IN_PROGRESS, TaskPriority.HIGH, 5);
    createTask(mobile, "Offline mode design", TaskStatus.TODO, TaskPriority.MEDIUM, 21);
    createTask(mobile, "App Store screenshots", TaskStatus.TODO, TaskPriority.LOW, 30);

    createTask(legacy, "Final data export", TaskStatus.DONE, TaskPriority.HIGH, -30);
  }

  private Organization createOrg(String name, String slug) {
    return organizationRepository.save(Organization.builder().name(name).slug(slug).build());
  }

  private void createUser(
      String email, String firstName, String lastName, Organization org, String roleName) {
    Role role = roleRepository.findByName(roleName);
    if (role == null) {
      throw new IllegalStateException("Role " + roleName + " not seeded yet");
    }
    AppUserDto dto = new AppUserDto();
    dto.setEmail(email);
    dto.setFirstName(firstName);
    dto.setLastName(lastName);
    dto.setPassword(DEMO_PASSWORD);
    dto.setEnabled(true);
    dto.setRoles(Set.of(role.getName()));
    dto.setOrganizationId(org.getId());
    userService.create(dto);
  }

  private Project createProject(
      Organization org, String name, String description, ProjectStatus status) {
    return projectRepository.save(
        Project.builder()
            .name(name)
            .description(description)
            .status(status)
            .organization(org)
            .build());
  }

  private void createTask(
      Project project, String name, TaskStatus status, TaskPriority priority, int dueInDays) {
    taskRepository.save(
        Task.builder()
            .name(name)
            .description(name + " for " + project.getName())
            .status(status)
            .priority(priority)
            .dueDate(LocalDate.now().plusDays(dueInDays))
            .project(project)
            .build());
  }
}
