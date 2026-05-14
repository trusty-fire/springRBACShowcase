package org.craftedcode.backend.repository;

import java.util.Collection;
import java.util.List;
import org.craftedcode.backend.model.Task;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TaskRepository extends AbstractRepository<Task> {

  @Query(
      "SELECT t.project.id AS parentId, t.id AS childId "
          + "FROM Task t WHERE t.project.id IN :projectIds")
  List<ParentChildIds> findIdsByProjectIds(@Param("projectIds") Collection<Long> projectIds);
}
