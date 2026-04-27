package org.craftedcode.backend.repository;

import de.frachtwerk.essencium.backend.repository.BaseUserRepository;
import org.craftedcode.backend.model.User;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends BaseUserRepository<User, Long> {

  @Modifying(clearAutomatically = true)
  @Transactional
  @Query("UPDATE User u SET u.organization = null WHERE u.organization.id = :orgId")
  void detachUsersFromOrganization(@Param("orgId") Long orgId);
}
