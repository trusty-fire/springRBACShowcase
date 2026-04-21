package org.craftedcode.backend.model.representation;

import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.craftedcode.backend.model.TaskPriority;
import org.craftedcode.backend.model.TaskStatus;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class TaskRepresentation extends AbstractRepresentationModel {

  private String description;

  private TaskStatus status;

  private TaskPriority priority;

  private LocalDate dueDate;

  private Long projectId;
}
