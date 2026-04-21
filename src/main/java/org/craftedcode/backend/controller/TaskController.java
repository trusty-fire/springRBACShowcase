package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.access.ExposesEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.dto.TaskDto;
import org.craftedcode.backend.model.representation.TaskRepresentation;
import org.craftedcode.backend.repository.specification.TaskSpecification;
import org.craftedcode.backend.service.TaskService;
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
}
