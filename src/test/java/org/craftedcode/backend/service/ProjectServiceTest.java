package org.craftedcode.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.representation.ProjectRepresentation;
import org.craftedcode.backend.model.representation.assembler.ProjectAssembler;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ParentChildIds;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.TaskRepository;
import org.craftedcode.backend.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class ProjectServiceTest {

  @Mock private ProjectRepository projectRepository;
  @Mock private OrganizationRepository organizationRepository;
  @Mock private TaskRepository taskRepository;
  @Mock private TenantContext tenantContext;

  private ProjectService target;

  @BeforeEach
  void setUp() {
    target =
        new ProjectService(
            projectRepository,
            new ProjectAssembler(),
            organizationRepository,
            taskRepository,
            tenantContext);
  }

  private static ParentChildIds row(Long parentId, Long childId) {
    return new ParentChildIds() {
      @Override
      public Long getParentId() {
        return parentId;
      }

      @Override
      public Long getChildId() {
        return childId;
      }
    };
  }

  private static Project project(Long id) {
    Organization org = Organization.builder().id(99L).name("Acme Inc").slug("acme").build();
    return Project.builder().id(id).name("Project " + id).organization(org).build();
  }

  @Test
  void toOutput_single_populatesBatchedTaskIds() {
    when(taskRepository.findIdsByProjectIds(anyList()))
        .thenReturn(List.of(row(1L, 10L), row(1L, 11L)));

    ProjectRepresentation result = target.toOutput(project(1L));

    assertThat(result.getTaskIds()).containsExactly(10L, 11L);
  }

  @Test
  void toOutput_page_batchLoadsTaskIdsWithOneQuery() {
    Page<Project> page = new PageImpl<>(List.of(project(1L), project(2L), project(3L)));
    when(taskRepository.findIdsByProjectIds(anyList()))
        .thenReturn(List.of(row(1L, 10L), row(2L, 20L), row(2L, 21L)));

    Page<ProjectRepresentation> result = target.toOutput(page);

    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getContent().get(0).getTaskIds()).containsExactly(10L);
    assertThat(result.getContent().get(1).getTaskIds()).containsExactly(20L, 21L);
    assertThat(result.getContent().get(2).getTaskIds()).isEmpty();
    // N+1 guard: one query regardless of page size
    verify(taskRepository, times(1)).findIdsByProjectIds(any());
  }

  @Test
  void toOutput_list_batchLoadsTaskIdsWithOneQuery() {
    when(taskRepository.findIdsByProjectIds(anyList())).thenReturn(List.of(row(1L, 10L)));

    List<ProjectRepresentation> result = target.toOutput(List.of(project(1L), project(2L)));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getTaskIds()).containsExactly(10L);
    assertThat(result.get(1).getTaskIds()).isEmpty();
    verify(taskRepository, times(1)).findIdsByProjectIds(any());
  }

  @Test
  void toOutput_nullPage_returnsNull() {
    assertThat(target.toOutput((Page<Project>) null)).isNull();
  }
}
