package org.craftedcode.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToOne;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Project extends AbstractModel {

  private String description;

  @Enumerated private ProjectStatus status;

  @ManyToOne private Organization organization;

  @Override
  public String getTitle() {
    return name + " - " + status;
  }
}
