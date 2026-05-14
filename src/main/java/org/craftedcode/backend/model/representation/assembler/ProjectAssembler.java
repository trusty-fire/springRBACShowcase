package org.craftedcode.backend.model.representation.assembler;

import java.util.List;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.springframework.stereotype.Component;

@Component
public class ProjectAssembler extends AbstractAssembler<Project, ProjectRepresentation> {

  /**
   * Bare mapping without related collections. {@link
   * org.craftedcode.backend.service.ProjectService} batch-loads relations and uses the enriched
   * overload instead.
   */
  @Override
  public ProjectRepresentation toRepresentation(Project entity) {
    return toRepresentation(entity, List.of());
  }

  public ProjectRepresentation toRepresentation(Project entity, List<Long> taskIds) {
    return ProjectRepresentation.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .status(entity.getStatus())
        .organizationId(entity.getOrganization().getId())
        .taskIds(taskIds)
        .build();
  }
}
