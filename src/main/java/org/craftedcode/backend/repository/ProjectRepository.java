package org.craftedcode.backend.repository;

import java.util.Collection;
import java.util.List;
import org.craftedcode.backend.model.Project;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProjectRepository extends AbstractRepository<Project> {

  @Query(
      "SELECT p.organization.id AS parentId, p.id AS childId "
          + "FROM Project p WHERE p.organization.id IN :orgIds")
  List<ParentChildIds> findIdsByOrganizationIds(@Param("orgIds") Collection<Long> orgIds);
}
