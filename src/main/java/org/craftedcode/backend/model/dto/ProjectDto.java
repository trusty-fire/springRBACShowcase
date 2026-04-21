package org.craftedcode.backend.model.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.craftedcode.backend.model.ProjectStatus;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class ProjectDto extends AbstractDto {

  private String description;

  private ProjectStatus status;

  private Long organizationId;
}
