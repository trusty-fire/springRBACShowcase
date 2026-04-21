package org.craftedcode.backend.service;

import java.util.Optional;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.dto.OrganizationDto;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.craftedcode.backend.model.representation.assembler.OrganizationAssembler;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService
    extends AbstractAssemblingEntityService<
        Organization, OrganizationDto, OrganizationRepresentation> {

  protected OrganizationService(
      OrganizationRepository repository, OrganizationAssembler assembler) {
    super(repository, assembler);
  }

  @Override
  protected <E extends OrganizationDto> Organization convertDtoToEntity(
      E dto, Optional<Organization> currentEntityOpt) {
    return Organization.builder().id(dto.getId()).name(dto.getName()).slug(dto.getSlug()).build();
  }
}
