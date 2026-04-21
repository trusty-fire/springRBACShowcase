package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.access.ExposesEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.dto.ProjectDto;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.craftedcode.backend.repository.specification.ProjectSpecification;
import org.craftedcode.backend.service.ProjectService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/projects")
@ExposesEntity(Project.class)
@Tag(
    name = "ProjectController",
    description = "REST-Endpoint for Project-Entity. Query-parameter available to all GET-Methods.")
public class ProjectController
    extends AbstractRestController<
        Project, ProjectDto, ProjectRepresentation, ProjectSpecification> {
  protected ProjectController(ProjectService projectService) {
    super(projectService);
  }
}
