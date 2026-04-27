package org.craftedcode.backend.model;

import de.frachtwerk.essencium.backend.model.AbstractBaseUser;
import jakarta.persistence.*;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@SuperBuilder(toBuilder = true)
public class User extends AbstractBaseUser<Long> {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // SET_NULL is the DB-level fallback so the FK is never left dangling; OrganizationService
  // additionally runs a JPA UPDATE before delete to keep Hibernate's first-level cache in sync.
  @ManyToOne
  @OnDelete(action = OnDeleteAction.SET_NULL)
  private Organization organization;

  public String getFullName() {
    return getFirstName() + " " + getLastName();
  }

  @Override
  public String getTitle() {
    return getFullName();
  }
}
