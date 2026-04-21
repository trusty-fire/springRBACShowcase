package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.access.ExposesEntity;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.dto.OrganizationDto;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.craftedcode.backend.repository.specification.OrganizationSpecification;
import org.craftedcode.backend.service.OrganizationService;
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
}
