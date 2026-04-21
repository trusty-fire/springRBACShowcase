package org.craftedcode.backend.model.representation.assembler;

import lombok.RequiredArgsConstructor;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrganizationAssembler
    extends AbstractAssembler<Organization, OrganizationRepresentation> {

  @Override
  public OrganizationRepresentation toRepresentation(Organization entity) {
    return OrganizationRepresentation.builder()
        .id(entity.getId())
        .name(entity.getName())
        .slug(entity.getSlug())
        // .userIds()
        .build();
  }
}
