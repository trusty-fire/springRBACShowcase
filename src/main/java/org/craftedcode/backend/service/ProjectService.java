package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import java.util.Optional;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.dto.ProjectDto;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.craftedcode.backend.model.representation.assembler.ProjectAssembler;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ProjectRepository;
import org.springframework.stereotype.Service;

@Service
public class ProjectService
    extends AbstractAssemblingEntityService<Project, ProjectDto, ProjectRepresentation> {

  private final OrganizationRepository organizationRepository;

  protected ProjectService(
      ProjectRepository repository,
      ProjectAssembler assembler,
      OrganizationRepository organizationRepository) {
    super(repository, assembler);
    this.organizationRepository = organizationRepository;
  }

  @Override
  protected <E extends ProjectDto> Project convertDtoToEntity(
      E dto, Optional<Project> currentEntityOpt) {
    return Project.builder()
        .id(dto.getId())
        .name(dto.getName())
        .description(dto.getDescription())
        .status(dto.getStatus())
        .organization(
            organizationRepository
                .findById(dto.getOrganizationId())
                .orElseThrow(ResourceNotFoundException::new))
        .build();
  }
}
