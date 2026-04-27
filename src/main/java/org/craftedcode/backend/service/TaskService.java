package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.dto.TaskDto;
import org.craftedcode.backend.model.representation.TaskRepresentation;
import org.craftedcode.backend.model.representation.assembler.TaskAssembler;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.TaskRepository;
import org.craftedcode.backend.repository.specification.TenantSpecifications;
import org.craftedcode.backend.security.TenantContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class TaskService
    extends AbstractAssemblingEntityService<Task, TaskDto, TaskRepresentation> {

  private final ProjectRepository projectRepository;
  private final TenantContext tenantContext;

  protected TaskService(
      TaskRepository repository,
      TaskAssembler assembler,
      ProjectRepository projectRepository,
      TenantContext tenantContext) {
    super(repository, assembler);
    this.projectRepository = projectRepository;
    this.tenantContext = tenantContext;
  }

  @Override
  protected Specification<Task> specificationPreProcessing(Specification<Task> spec) {
    Organization org = tenantContext.requireCurrentOrganization();
    Specification<Task> tenantSpec = TenantSpecifications.taskBelongsToOrg(org);
    return spec == null ? tenantSpec : tenantSpec.and(spec);
  }

  @Override
  protected <E extends TaskDto> Task createPreProcessing(@NotNull E dto) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    Project project =
        projectRepository.findById(dto.getProjectId()).orElseThrow(ResourceNotFoundException::new);
    if (!Objects.equals(callerOrg.getId(), project.getOrganization().getId())) {
      throw new ResourceNotFoundException();
    }
    return super.createPreProcessing(dto);
  }

  @Override
  protected <E extends TaskDto> Task updatePreProcessing(@NotNull Long id, @NotNull E dto) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    Task existing = repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    if (!Objects.equals(callerOrg.getId(), existing.getProject().getOrganization().getId())) {
      throw new ResourceNotFoundException();
    }
    Project newProject =
        projectRepository.findById(dto.getProjectId()).orElseThrow(ResourceNotFoundException::new);
    if (!Objects.equals(callerOrg.getId(), newProject.getOrganization().getId())) {
      throw new ResourceNotFoundException();
    }
    return super.updatePreProcessing(id, dto);
  }

  @Override
  protected void deletePreProcessing(@NotNull Long id) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    Task existing = repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    if (!Objects.equals(callerOrg.getId(), existing.getProject().getOrganization().getId())) {
      throw new ResourceNotFoundException();
    }
    super.deletePreProcessing(id);
  }

  @Override
  protected <E extends TaskDto> Task convertDtoToEntity(E dto, Optional<Task> currentEntityOpt) {
    return Task.builder()
        .id(dto.getId())
        .name(dto.getName())
        .description(dto.getDescription())
        .status(dto.getStatus())
        .priority(dto.getPriority())
        .dueDate(dto.getDueDate())
        .project(
            projectRepository
                .findById(dto.getProjectId())
                .orElseThrow(ResourceNotFoundException::new))
        .build();
  }
}
