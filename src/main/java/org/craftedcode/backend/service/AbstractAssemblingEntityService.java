package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.IdentityIdModel;
import de.frachtwerk.essencium.backend.service.AbstractEntityService;
import de.frachtwerk.essencium.backend.service.AssemblingService;
import org.craftedcode.backend.repository.AbstractRepository;
import org.craftedcode.backend.model.representation.assembler.AbstractAssembler;
import lombok.Getter;

@Getter
public abstract class AbstractAssemblingEntityService<M extends IdentityIdModel, IN, OUT>
    extends AbstractEntityService<M, Long, IN> implements AssemblingService<M, OUT> {
  protected final AbstractAssembler<M, OUT> assembler;

  protected AbstractAssemblingEntityService(
      final AbstractRepository<M> repository,
      final AbstractAssembler<M, OUT> assembler) {
    super(repository);
    this.assembler = assembler;
  }
}
