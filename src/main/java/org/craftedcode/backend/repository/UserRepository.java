package org.craftedcode.backend.repository;

import de.frachtwerk.essencium.backend.repository.BaseUserRepository;
import org.craftedcode.backend.model.User;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends BaseUserRepository<User, Long> {}
