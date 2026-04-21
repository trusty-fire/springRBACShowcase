package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.AbstractAccessAwareController;
import de.frachtwerk.essencium.backend.model.Identifiable;
import de.frachtwerk.essencium.backend.model.IdentityIdModel;
import de.frachtwerk.essencium.backend.model.exception.InvalidInputException;
import de.frachtwerk.essencium.backend.model.representation.BasicRepresentation;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.util.*;
import java.util.stream.Collectors;
import net.kaczmarzyk.spring.data.jpa.domain.Equal;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.craftedcode.backend.service.AbstractAssemblingEntityService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.MethodNotAllowedException;

public abstract class AbstractRestController<
        M extends IdentityIdModel, I extends Identifiable<Long>, O, S extends Specification<M>>
    extends AbstractAccessAwareController<M, Long, I, O, S> {

  private final AbstractAssemblingEntityService<M, I, O> abstractAssemblingEntityService;

  protected AbstractRestController(AbstractAssemblingEntityService<M, I, O> service) {
    super(service);
    this.abstractAssemblingEntityService = service;
  }

  @Override
  public O toRepresentation(M entity) {
    return abstractAssemblingEntityService.toOutput(entity);
  }

  @Override
  public Page<O> toRepresentation(Page<M> page) {
    return abstractAssemblingEntityService.toOutput(page);
  }

  public List<O> toRepresentation(List<M> list) {
    return list.stream()
        .map(abstractAssemblingEntityService::toOutput)
        .collect(Collectors.toList());
  }

  @Override
  @GetMapping
  public Page<O> findAll(
      @Parameter(hidden = true) S specification,
      @PageableDefault(sort = "id", direction = Sort.Direction.DESC) Pageable pageable) {
    Specification<M> distinctSpec =
        ((root, query, criteriaBuilder) -> {
          assert query != null;
          query.distinct(true);
          return criteriaBuilder.isTrue(criteriaBuilder.literal(true));
        });
    if (Objects.isNull(specification)) {
      return super.findAll((S) distinctSpec, pageable);
    } else {
      return super.findAll((S) specification.and(distinctSpec), pageable);
    }
  }

  @GetMapping("/list")
  public List<O> findAllAsList(@Parameter(hidden = true) S specification) {
    return this.toRepresentation(this.service.getAllFiltered(specification));
  }

  @Override
  @GetMapping("/{id}")
  public O findById(
      @Parameter(hidden = true) @Spec(path = "id", pathVars = "id", spec = Equal.class) S spec) {
    return super.findById(spec);
  }

  @Override
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public O create(@Valid @RequestBody @NotNull I input) {
    if (input.getId() != null) {
      throw new InvalidInputException("You cannot create an entity with a given ID.");
    }
    return super.create(input);
  }

  @Override
  @PutMapping(value = "/{id}")
  @ResponseStatus(HttpStatus.OK)
  public O update(
      @PathVariable @NotNull Long id,
      @Valid @RequestBody @NotNull I input,
      @Parameter(hidden = true) @Spec(path = "id", pathVars = "id", spec = Equal.class) S spec) {
    if (!id.equals(input.getId())) {
      throw new InvalidInputException("The ID in the path and the body must be the same.");
    }
    if (input.getId() == null) {
      throw new InvalidInputException(
          "You cannot update an entity without an ID in the request body.");
    }
    return super.update(id, input, spec);
  }

  @Override
  @Hidden
  @Parameter(hidden = true)
  @PatchMapping("/{id}")
  @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
  public final O update(
      @NotNull Long id, @NotNull Map<String, Object> userFields, @Parameter(hidden = true) S spec) {
    throw new MethodNotAllowedException(
        HttpMethod.PATCH,
        List.of(HttpMethod.GET, HttpMethod.POST, HttpMethod.PUT, HttpMethod.DELETE));
  }

  @Override
  @DeleteMapping(value = "/{id}")
  @Operation(
      summary = "delete",
      description = "Deletes the entity with the given ID. The ID must be provided in the path.")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void delete(
      @PathVariable @NotNull Long id,
      @Parameter(hidden = true) @Spec(path = "id", pathVars = "id", spec = Equal.class) S spec) {
    super.delete(id, spec);
  }

  @Override
  @Operation(
      summary = "getBasicEntities",
      description =
          "REST-Endpoint for every Entity. Delivers basic objects containing ID and Title of an"
              + " entity. All filters of the regular GET-endpoint are applicable.")
  @GetMapping("/basic")
  public List<BasicRepresentation> findAll(@Parameter(hidden = true) S specification) {
    List<BasicRepresentation> result = new ArrayList<>(super.findAll(specification));
    result.sort(Comparator.comparing(BasicRepresentation::name, String.CASE_INSENSITIVE_ORDER));
    return result;
  }
}
