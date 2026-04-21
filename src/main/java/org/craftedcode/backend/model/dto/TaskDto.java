package org.craftedcode.backend.model.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.craftedcode.backend.model.TaskPriority;
import org.craftedcode.backend.model.TaskStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class TaskDto extends AbstractDto {

  private String description;

  private TaskStatus status;

  private TaskPriority priority;

  private LocalDate dueDate;

  private Long projectId;
}
