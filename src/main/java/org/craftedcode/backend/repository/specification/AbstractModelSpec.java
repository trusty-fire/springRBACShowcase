package org.craftedcode.backend.repository.specification;

import de.frachtwerk.essencium.backend.model.IdentityIdModel;
import de.frachtwerk.essencium.backend.repository.specification.BaseModelSpec;

public interface AbstractModelSpec<T extends IdentityIdModel> extends BaseModelSpec<T, Long> {}
