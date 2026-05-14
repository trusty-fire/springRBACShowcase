package org.craftedcode.backend.model.representation.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.ProjectStatus;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProjectAssemblerTest {

  @InjectMocks private ProjectAssembler target;

  private static Project project() {
    Organization org = Organization.builder().id(99L).name("Acme Inc").slug("acme").build();
    return Project.builder()
        .id(1L)
        .name("Website Relaunch")
        .description("Marketing site redesign")
        .status(ProjectStatus.ACTIVE)
        .organization(org)
        .build();
  }

  @Test
  void toRepresentation_bare_mapsScalarsAndEmptyRelations() {
    ProjectRepresentation result = target.toRepresentation(project());

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Website Relaunch");
    assertThat(result.getDescription()).isEqualTo("Marketing site redesign");
    assertThat(result.getStatus()).isEqualTo(ProjectStatus.ACTIVE);
    assertThat(result.getOrganizationId()).isEqualTo(99L);
    assertThat(result.getTaskIds()).isEmpty();
  }

  @Test
  void toRepresentation_enriched_populatesTaskIds() {
    ProjectRepresentation result = target.toRepresentation(project(), List.of(1L, 2L, 3L));

    assertThat(result.getTaskIds()).containsExactly(1L, 2L, 3L);
  }

  @Test
  void toModel_delegatesToBareRepresentation() {
    Project project = project();

    assertThat(target.toModel(project)).isEqualTo(target.toRepresentation(project));
  }
}
