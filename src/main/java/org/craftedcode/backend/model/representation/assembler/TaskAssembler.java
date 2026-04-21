package org.craftedcode.backend.model.representation.assembler;

import lombok.RequiredArgsConstructor;
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.representation.TaskRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class TaskAssembler extends AbstractAssembler<Task, TaskRepresentation> {

  @Override
  public TaskRepresentation toRepresentation(Task entity) {
    return TaskRepresentation.builder()
        .id(entity.getId())
        .name(entity.getName())
        .description(entity.getDescription())
        .status(entity.getStatus())
        .priority(entity.getPriority())
        .dueDate(entity.getDueDate())
        .projectId(entity.getProject().getId())
        .build();
  }
}
