package org.craftedcode.backend.model.representation;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class OrganizationRepresentation extends AbstractRepresentationModel {

  private String slug;

  private List<Long> projectIds;

  private List<Long> userIds;
}
