package org.craftedcode.backend.model.representation.assembler;

import lombok.RequiredArgsConstructor;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ProjectAssembler extends AbstractAssembler<Project, ProjectRepresentation> {

  @Override
  public ProjectRepresentation toRepresentation(Project entity) {
    return ProjectRepresentation.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .status(entity.getStatus())
        .organizationId(entity.getOrganization().getId())
        // .taskIds()
        .build();
  }
}
