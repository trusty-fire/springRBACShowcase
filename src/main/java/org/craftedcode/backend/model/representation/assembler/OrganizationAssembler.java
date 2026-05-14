package org.craftedcode.backend.model.representation.assembler;

import java.util.List;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.springframework.stereotype.Component;

@Component
public class OrganizationAssembler
    extends AbstractAssembler<Organization, OrganizationRepresentation> {

  /**
   * Bare mapping without related collections. {@link
   * org.craftedcode.backend.service.OrganizationService} batch-loads relations and uses the
   * enriched overload instead.
   */
  @Override
  public OrganizationRepresentation toRepresentation(Organization entity) {
    return toRepresentation(entity, List.of(), List.of());
  }

  public OrganizationRepresentation toRepresentation(
      Organization entity, List<Long> projectIds, List<Long> userIds) {
    return OrganizationRepresentation.builder()
        .id(entity.getId())
        .name(entity.getName())
        .slug(entity.getSlug())
        .projectIds(projectIds)
        .userIds(userIds)
        .build();
  }
}
