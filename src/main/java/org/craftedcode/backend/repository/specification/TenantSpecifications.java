package org.craftedcode.backend.repository.specification;

import jakarta.persistence.criteria.Join;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.Project;
import org.craftedcode.backend.model.Task;
import org.craftedcode.backend.model.User;
import org.springframework.data.jpa.domain.Specification;

public final class TenantSpecifications {

  private TenantSpecifications() {}

  public static Specification<Organization> isOrganization(Organization org) {
    return (root, query, cb) -> cb.equal(root.get("id"), org.getId());
  }

  public static Specification<Project> projectBelongsToOrg(Organization org) {
    return (root, query, cb) -> cb.equal(root.get("organization").get("id"), org.getId());
  }

  public static Specification<Task> taskBelongsToOrg(Organization org) {
    return (root, query, cb) -> {
      Join<Task, Project> project = root.join("project");
      return cb.equal(project.get("organization").get("id"), org.getId());
    };
  }

  public static Specification<User> userBelongsToOrg(Organization org) {
    return (root, query, cb) -> cb.equal(root.get("organization").get("id"), org.getId());
  }
}
