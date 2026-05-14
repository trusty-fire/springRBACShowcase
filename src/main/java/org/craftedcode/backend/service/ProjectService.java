package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.dto.ProjectDto;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.craftedcode.backend.model.representation.assembler.ProjectAssembler;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.TaskRepository;
import org.craftedcode.backend.repository.specification.TenantSpecifications;
import org.craftedcode.backend.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class ProjectService
    extends AbstractAssemblingEntityService<Project, ProjectDto, ProjectRepresentation> {

  private final OrganizationRepository organizationRepository;
  private final TaskRepository taskRepository;
  private final TenantContext tenantContext;
  private final ProjectAssembler projectAssembler;

  protected ProjectService(
      ProjectRepository repository,
      ProjectAssembler assembler,
      OrganizationRepository organizationRepository,
      TaskRepository taskRepository,
      TenantContext tenantContext) {
    super(repository, assembler);
    this.organizationRepository = organizationRepository;
    this.taskRepository = taskRepository;
    this.tenantContext = tenantContext;
    this.projectAssembler = assembler;
  }

  @Override
  public ProjectRepresentation toOutput(Project entity) {
    return enricher(List.of(entity.getId())).apply(entity);
  }

  @Override
  public Page<ProjectRepresentation> toOutput(Page<Project> page) {
    if (page == null) {
      return null;
    }
    return page.map(enricher(page.getContent().stream().map(Project::getId).toList()));
  }

  @Override
  public List<ProjectRepresentation> toOutput(List<Project> entities) {
    Function<Project, ProjectRepresentation> enricher =
        enricher(entities.stream().map(Project::getId).toList());
    return entities.stream().map(enricher).toList();
  }

  /** Batch-loads task ids for the given projects, avoiding N+1 queries. */
  private Function<Project, ProjectRepresentation> enricher(List<Long> projectIds) {
    Map<Long, List<Long>> taskIds = groupByParent(taskRepository.findIdsByProjectIds(projectIds));
    return project ->
        projectAssembler.toRepresentation(
            project, taskIds.getOrDefault(project.getId(), List.of()));
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
