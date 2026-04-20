package org.craftedcode.backend.repository;

import de.frachtwerk.essencium.backend.model.IdentityIdModel;
import de.frachtwerk.essencium.backend.repository.BaseRepository;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface AbstractRepository<E extends IdentityIdModel> extends BaseRepository<E, Long> {}
