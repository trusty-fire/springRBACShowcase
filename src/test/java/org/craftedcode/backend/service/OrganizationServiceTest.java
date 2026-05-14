package org.craftedcode.backend.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.craftedcode.backend.model.representation.assembler.OrganizationAssembler;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ParentChildIds;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.UserRepository;
import org.craftedcode.backend.security.TenantContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

@ExtendWith(MockitoExtension.class)
class OrganizationServiceTest {

  @Mock private OrganizationRepository organizationRepository;
  @Mock private TenantContext tenantContext;
  @Mock private ProjectRepository projectRepository;
  @Mock private UserRepository userRepository;

  private OrganizationService target;

  @BeforeEach
  void setUp() {
    target =
        new OrganizationService(
            organizationRepository,
            new OrganizationAssembler(),
            tenantContext,
            projectRepository,
            userRepository);
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

  private static Organization org(Long id) {
    return Organization.builder().id(id).name("Org " + id).slug("org-" + id).build();
  }

  @Test
  void toOutput_single_populatesBatchedRelations() {
    when(projectRepository.findIdsByOrganizationIds(anyList()))
        .thenReturn(List.of(row(1L, 10L), row(1L, 11L)));
    when(userRepository.findIdsByOrganizationIds(anyList())).thenReturn(List.of(row(1L, 20L)));

    OrganizationRepresentation result = target.toOutput(org(1L));

    assertThat(result.getProjectIds()).containsExactly(10L, 11L);
    assertThat(result.getUserIds()).containsExactly(20L);
  }

  @Test
  void toOutput_page_batchLoadsEachRelationWithOneQuery() {
    Page<Organization> page = new PageImpl<>(List.of(org(1L), org(2L), org(3L)));
    when(projectRepository.findIdsByOrganizationIds(anyList()))
        .thenReturn(List.of(row(1L, 10L), row(2L, 20L), row(2L, 21L)));
    when(userRepository.findIdsByOrganizationIds(anyList())).thenReturn(List.of(row(3L, 30L)));

    Page<OrganizationRepresentation> result = target.toOutput(page);

    assertThat(result.getContent()).hasSize(3);
    assertThat(result.getContent().get(0).getProjectIds()).containsExactly(10L);
    assertThat(result.getContent().get(1).getProjectIds()).containsExactly(20L, 21L);
    assertThat(result.getContent().get(2).getProjectIds()).isEmpty();
    assertThat(result.getContent().get(2).getUserIds()).containsExactly(30L);
    // N+1 guard: one query per relation regardless of page size
    verify(projectRepository, times(1)).findIdsByOrganizationIds(any());
    verify(userRepository, times(1)).findIdsByOrganizationIds(any());
  }

  @Test
  void toOutput_list_batchLoadsEachRelationWithOneQuery() {
    when(projectRepository.findIdsByOrganizationIds(anyList())).thenReturn(List.of(row(1L, 10L)));
    when(userRepository.findIdsByOrganizationIds(anyList())).thenReturn(List.of(row(2L, 20L)));

    List<OrganizationRepresentation> result = target.toOutput(List.of(org(1L), org(2L)));

    assertThat(result).hasSize(2);
    assertThat(result.get(0).getProjectIds()).containsExactly(10L);
    assertThat(result.get(0).getUserIds()).isEmpty();
    assertThat(result.get(1).getUserIds()).containsExactly(20L);
    verify(projectRepository, times(1)).findIdsByOrganizationIds(any());
    verify(userRepository, times(1)).findIdsByOrganizationIds(any());
  }

  @Test
  void toOutput_nullPage_returnsNull() {
    assertThat(target.toOutput((Page<Organization>) null)).isNull();
  }
}
