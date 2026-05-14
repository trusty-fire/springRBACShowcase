package org.craftedcode.backend.model.representation.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.TaskPriority;
import org.craftedcode.backend.model.TaskStatus;
import org.craftedcode.backend.model.representation.TaskRepresentation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TaskAssemblerTest {

  @InjectMocks private TaskAssembler target;

  @Test
  void toRepresentation_mapsAllFields() {
    Project project = Project.builder().id(42L).name("Website Relaunch").build();
    LocalDate dueDate = LocalDate.of(2026, 6, 1);
    Task task =
        Task.builder()
            .id(1L)
            .name("Design homepage")
            .description("New hero section")
            .status(TaskStatus.IN_PROGRESS)
            .priority(TaskPriority.HIGH)
            .dueDate(dueDate)
            .project(project)
            .build();

    TaskRepresentation result = target.toRepresentation(task);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Design homepage");
    assertThat(result.getDescription()).isEqualTo("New hero section");
    assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
    assertThat(result.getPriority()).isEqualTo(TaskPriority.HIGH);
    assertThat(result.getDueDate()).isEqualTo(dueDate);
    assertThat(result.getProjectId()).isEqualTo(42L);
  }

  @Test
  void toModel_delegatesToRepresentation() {
    Project project = Project.builder().id(42L).name("Website Relaunch").build();
    Task task = Task.builder().id(1L).name("Design homepage").project(project).build();

    assertThat(target.toModel(task)).isEqualTo(target.toRepresentation(task));
  }
}
