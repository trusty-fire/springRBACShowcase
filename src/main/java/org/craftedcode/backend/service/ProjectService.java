package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.dto.ProjectDto;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.craftedcode.backend.model.representation.assembler.ProjectAssembler;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.specification.TenantSpecifications;
import org.craftedcode.backend.security.TenantContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ProjectService
    extends AbstractAssemblingEntityService<Project, ProjectDto, ProjectRepresentation> {

  private final OrganizationRepository organizationRepository;
  private final TenantContext tenantContext;

  protected ProjectService(
      ProjectRepository repository,
      ProjectAssembler assembler,
      OrganizationRepository organizationRepository,
      TenantContext tenantContext) {
    super(repository, assembler);
    this.organizationRepository = organizationRepository;
    this.tenantContext = tenantContext;
  }

  @Override
  protected Specification<Project> specificationPreProcessing(Specification<Project> spec) {
    Organization org = tenantContext.requireCurrentOrganization();
    Specification<Project> tenantSpec = TenantSpecifications.projectBelongsToOrg(org);
    return spec == null ? tenantSpec : tenantSpec.and(spec);
  }

  @Override
  protected <E extends ProjectDto> Project createPreProcessing(@NotNull E dto) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    Organization targetOrg =
        organizationRepository
            .findById(dto.getOrganizationId())
            .orElseThrow(ResourceNotFoundException::new);
    if (!Objects.equals(callerOrg.getId(), targetOrg.getId())) {
      throw new ResourceNotFoundException();
    }
    return super.createPreProcessing(dto);
  }

  @Override
  protected <E extends ProjectDto> Project updatePreProcessing(@NotNull Long id, @NotNull E dto) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    Project existing = repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    if (!Objects.equals(callerOrg.getId(), existing.getOrganization().getId())) {
      throw new ResourceNotFoundException();
    }
    Organization targetOrg =
        organizationRepository
            .findById(dto.getOrganizationId())
            .orElseThrow(ResourceNotFoundException::new);
    if (!Objects.equals(callerOrg.getId(), targetOrg.getId())) {
      throw new ResourceNotFoundException();
    }
    return super.updatePreProcessing(id, dto);
  }

  @Override
  protected void deletePreProcessing(@NotNull Long id) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    Project existing = repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    if (!Objects.equals(callerOrg.getId(), existing.getOrganization().getId())) {
      throw new ResourceNotFoundException();
    }
    super.deletePreProcessing(id);
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
