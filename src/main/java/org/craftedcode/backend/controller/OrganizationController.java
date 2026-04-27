package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.access.ExposesEntity;
import de.frachtwerk.essencium.backend.model.representation.BasicRepresentation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.dto.OrganizationDto;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.craftedcode.backend.repository.specification.OrganizationSpecification;
import org.craftedcode.backend.service.OrganizationService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/v1/organizations")
@ExposesEntity(Organization.class)
@Tag(
    name = "OrganizationController",
    description =
        "REST-Endpoint for Organization-Entity. Query-parameter available to all GET-Methods.")
public class OrganizationController
    extends AbstractRestController<
        Organization, OrganizationDto, OrganizationRepresentation, OrganizationSpecification> {
  protected OrganizationController(OrganizationService organizationService) {
    super(organizationService);
  }

  @Override
  @Secured({"ORGANIZATION_READ"})
  public Page<OrganizationRepresentation> findAll(
      OrganizationSpecification specification, Pageable pageable) {
    return super.findAll(specification, pageable);
  }

  @Override
  @Secured({"ORGANIZATION_READ"})
  public List<OrganizationRepresentation> findAllAsList(OrganizationSpecification specification) {
    return super.findAllAsList(specification);
  }

  @Override
  @Secured({"ORGANIZATION_READ"})
  public List<BasicRepresentation> findAll(OrganizationSpecification specification) {
    return super.findAll(specification);
  }

  @Override
  @Secured({"ORGANIZATION_READ"})
  public OrganizationRepresentation findById(OrganizationSpecification spec) {
    return super.findById(spec);
  }

  @Override
  @Secured({"ORGANIZATION_CREATE"})
  public OrganizationRepresentation create(OrganizationDto input) {
    return super.create(input);
  }

  @Override
  @Secured({"ORGANIZATION_UPDATE"})
  public OrganizationRepresentation update(
      Long id, OrganizationDto input, OrganizationSpecification spec) {
    return super.update(id, input, spec);
  }

  @Override
  @Secured({"ORGANIZATION_DELETE"})
  public void delete(Long id, OrganizationSpecification spec) {
    super.delete(id, spec);
  }
}
