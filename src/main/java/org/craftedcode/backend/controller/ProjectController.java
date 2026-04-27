package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.access.ExposesEntity;
import de.frachtwerk.essencium.backend.model.representation.BasicRepresentation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.dto.ProjectDto;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.craftedcode.backend.repository.specification.ProjectSpecification;
import org.craftedcode.backend.service.ProjectService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
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

  @Override
  @Secured({"PROJECT_READ"})
  public Page<ProjectRepresentation> findAll(
      ProjectSpecification specification, Pageable pageable) {
    return super.findAll(specification, pageable);
  }

  @Override
  @Secured({"PROJECT_READ"})
  public List<ProjectRepresentation> findAllAsList(ProjectSpecification specification) {
    return super.findAllAsList(specification);
  }

  @Override
  @Secured({"PROJECT_READ"})
  public List<BasicRepresentation> findAll(ProjectSpecification specification) {
    return super.findAll(specification);
  }

  @Override
  @Secured({"PROJECT_READ"})
  public ProjectRepresentation findById(ProjectSpecification spec) {
    return super.findById(spec);
  }

  @Override
  @Secured({"PROJECT_CREATE"})
  public ProjectRepresentation create(ProjectDto input) {
    return super.create(input);
  }

  @Override
  @Secured({"PROJECT_UPDATE"})
  public ProjectRepresentation update(Long id, ProjectDto input, ProjectSpecification spec) {
    return super.update(id, input, spec);
  }

  @Override
  @Secured({"PROJECT_DELETE"})
  public void delete(Long id, ProjectSpecification spec) {
    super.delete(id, spec);
  }
}
