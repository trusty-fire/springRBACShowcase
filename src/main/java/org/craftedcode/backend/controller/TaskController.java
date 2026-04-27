package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.access.ExposesEntity;
import de.frachtwerk.essencium.backend.model.representation.BasicRepresentation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.dto.TaskDto;
import org.craftedcode.backend.model.representation.TaskRepresentation;
import org.craftedcode.backend.repository.specification.TaskSpecification;
import org.craftedcode.backend.service.TaskService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/tasks")
@ExposesEntity(Task.class)
@Tag(
    name = "TaskController",
    description = "REST-Endpoint for Task-Entity. Query-parameter available to all GET-Methods.")
public class TaskController
    extends AbstractRestController<Task, TaskDto, TaskRepresentation, TaskSpecification> {
  protected TaskController(TaskService taskService) {
    super(taskService);
  }

  @Override
  @Secured({"TASK_READ"})
  public Page<TaskRepresentation> findAll(TaskSpecification specification, Pageable pageable) {
    return super.findAll(specification, pageable);
  }

  @Override
  @Secured({"TASK_READ"})
  public List<TaskRepresentation> findAllAsList(TaskSpecification specification) {
    return super.findAllAsList(specification);
  }

  @Override
  @Secured({"TASK_READ"})
  public List<BasicRepresentation> findAll(TaskSpecification specification) {
    return super.findAll(specification);
  }

  @Override
  @Secured({"TASK_READ"})
  public TaskRepresentation findById(TaskSpecification spec) {
    return super.findById(spec);
  }

  @Override
  @Secured({"TASK_CREATE"})
  public TaskRepresentation create(TaskDto input) {
    return super.create(input);
  }

  @Override
  @Secured({"TASK_UPDATE"})
  public TaskRepresentation update(Long id, TaskDto input, TaskSpecification spec) {
    return super.update(id, input, spec);
  }

  @Override
  @Secured({"TASK_DELETE"})
  public void delete(Long id, TaskSpecification spec) {
    super.delete(id, spec);
  }
}
