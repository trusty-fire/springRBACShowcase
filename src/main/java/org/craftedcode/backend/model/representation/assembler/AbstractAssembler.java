package org.craftedcode.backend.model.representation.assembler;

import de.frachtwerk.essencium.backend.model.AbstractBaseModel;
import de.frachtwerk.essencium.backend.model.representation.assembler.AbstractRepresentationAssembler;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractAssembler<M extends AbstractBaseModel<Long>, O>
    extends AbstractRepresentationAssembler<M, O> {

  @NotNull
  public abstract O toRepresentation(@NotNull M entity);

  @Override
  public final @NotNull O toModel(@NotNull M entity) {
    return toRepresentation(entity);
  }
}
