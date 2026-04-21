package org.craftedcode.backend.model;

import jakarta.persistence.Entity;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class Organization extends AbstractModel {

  private String slug;

  @Override
  public String getTitle() {
    return slug + " - " + name;
  }
}
