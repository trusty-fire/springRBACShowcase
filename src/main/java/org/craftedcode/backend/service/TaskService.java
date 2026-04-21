package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import java.util.Optional;
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.dto.TaskDto;
import org.craftedcode.backend.model.representation.TaskRepresentation;
import org.craftedcode.backend.model.representation.assembler.TaskAssembler;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.TaskRepository;
import org.springframework.stereotype.Service;

@Service
public class TaskService
    extends AbstractAssemblingEntityService<Task, TaskDto, TaskRepresentation> {

  private final ProjectRepository projectRepository;

  protected TaskService(
      TaskRepository repository, TaskAssembler assembler, ProjectRepository projectRepository) {
    super(repository, assembler);
    this.projectRepository = projectRepository;
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
