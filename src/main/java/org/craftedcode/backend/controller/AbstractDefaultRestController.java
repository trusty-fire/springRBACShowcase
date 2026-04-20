package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.AbstractAccessAwareController;
import de.frachtwerk.essencium.backend.model.Identifiable;
import de.frachtwerk.essencium.backend.model.IdentityIdModel;
import org.craftedcode.backend.service.AbstractAssemblingEntityService;
import org.springframework.data.jpa.domain.Specification;

public abstract class AbstractDefaultRestController<
        M extends IdentityIdModel, IN extends Identifiable<Long>, OUT, S extends Specification<M>>
        extends AbstractAccessAwareController<M, Long, IN, OUT, S> {

    protected AbstractDefaultRestController(AbstractAssemblingEntityService<M, IN, OUT> service) {
        super(service);
    }
}
