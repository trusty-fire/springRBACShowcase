package org.craftedcode.backend.model.representation;

import java.util.List;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.experimental.SuperBuilder;
import org.craftedcode.backend.model.ProjectStatus;

@Data
@SuperBuilder
@EqualsAndHashCode(callSuper = true)
public class ProjectRepresentation extends AbstractRepresentationModel {

  private String description;

  private ProjectStatus status;

  private Long organizationId;

  private List<Long> taskIds;
}
