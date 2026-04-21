package org.craftedcode.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Task extends AbstractModel {

  private String description;

  @Enumerated private TaskStatus status;

  @Enumerated private TaskPriority priority;

  private LocalDate dueDate;

  @ManyToOne private Project project;

  @Override
  public String getTitle() {
    return name + " - " + status;
  }
}
