package org.craftedcode.backend.model.representation.assembler;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrganizationAssemblerTest {

  @InjectMocks private OrganizationAssembler target;

  @Test
  void toRepresentation_bare_mapsScalarsAndEmptyRelations() {
    Organization org = Organization.builder().id(1L).name("Acme Inc").slug("acme").build();

    OrganizationRepresentation result = target.toRepresentation(org);

    assertThat(result.getId()).isEqualTo(1L);
    assertThat(result.getName()).isEqualTo("Acme Inc");
    assertThat(result.getSlug()).isEqualTo("acme");
    assertThat(result.getProjectIds()).isEmpty();
    assertThat(result.getUserIds()).isEmpty();
  }

  @Test
  void toRepresentation_enriched_populatesRelationCollections() {
    Organization org = Organization.builder().id(1L).name("Acme Inc").slug("acme").build();

    OrganizationRepresentation result =
        target.toRepresentation(org, List.of(10L, 11L), List.of(20L, 21L, 22L));

    assertThat(result.getProjectIds()).containsExactly(10L, 11L);
    assertThat(result.getUserIds()).containsExactly(20L, 21L, 22L);
  }

  @Test
  void toModel_delegatesToBareRepresentation() {
    Organization org = Organization.builder().id(1L).name("Acme Inc").slug("acme").build();

    assertThat(target.toModel(org)).isEqualTo(target.toRepresentation(org));
  }
}
