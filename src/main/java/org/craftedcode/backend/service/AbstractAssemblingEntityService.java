package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.IdentityIdModel;
import de.frachtwerk.essencium.backend.service.AbstractEntityService;
import de.frachtwerk.essencium.backend.service.AssemblingService;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.Getter;
import org.craftedcode.backend.model.representation.assembler.AbstractAssembler;
import org.craftedcode.backend.repository.AbstractRepository;
import org.craftedcode.backend.repository.ParentChildIds;

@Getter
public abstract class AbstractAssemblingEntityService<M extends IdentityIdModel, IN, OUT>
    extends AbstractEntityService<M, Long, IN> implements AssemblingService<M, OUT> {
  protected final AbstractAssembler<M, OUT> assembler;

  protected AbstractAssemblingEntityService(
      final AbstractRepository<M> repository, final AbstractAssembler<M, OUT> assembler) {
    super(repository);
    this.assembler = assembler;
  }

  /**
   * Converts a list of entities to representations. Subclasses that resolve related entities should
   * override this to batch-load those relations and avoid N+1 queries.
   */
  public List<OUT> toOutput(List<M> entities) {
    return entities.stream().map(this::toOutput).collect(Collectors.toList());
  }

  /** Groups child ids by their parent id for batch-loaded relations. */
  protected static Map<Long, List<Long>> groupByParent(List<ParentChildIds> rows) {
    return rows.stream()
        .collect(
            Collectors.groupingBy(
                ParentChildIds::getParentId,
                Collectors.mapping(ParentChildIds::getChildId, Collectors.toList())));
  }
}
