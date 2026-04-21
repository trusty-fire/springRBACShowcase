package org.craftedcode.backend.model;

import de.frachtwerk.essencium.backend.model.AbstractBaseUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class User extends AbstractBaseUser<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne private Organization organization;

  @Enumerated private OrgRole orgRole;

  public String getFullName() {
    return getFirstName() + " " + getLastName();
  }

  @Override
  public String getTitle() {
    return getFullName();
  }
}
