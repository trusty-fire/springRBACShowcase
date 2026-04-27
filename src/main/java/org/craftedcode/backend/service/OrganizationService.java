package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.exception.NotAllowedException;
import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.dto.OrganizationDto;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.craftedcode.backend.model.representation.assembler.OrganizationAssembler;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.UserRepository;
import org.craftedcode.backend.repository.specification.TenantSpecifications;
import org.craftedcode.backend.security.TenantContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService
    extends AbstractAssemblingEntityService<
        Organization, OrganizationDto, OrganizationRepresentation> {

  private final TenantContext tenantContext;
  private final UserRepository userRepository;

  protected OrganizationService(
      OrganizationRepository repository,
      OrganizationAssembler assembler,
      TenantContext tenantContext,
      UserRepository userRepository) {
    super(repository, assembler);
    this.tenantContext = tenantContext;
    this.userRepository = userRepository;
  }

  @Override
  protected Specification<Organization> specificationPreProcessing(
      Specification<Organization> spec) {
    Organization org = tenantContext.requireCurrentOrganization();
    Specification<Organization> tenantSpec = TenantSpecifications.isOrganization(org);
    return spec == null ? tenantSpec : tenantSpec.and(spec);
  }

  @Override
  protected <E extends OrganizationDto> Organization createPreProcessing(@NotNull E dto) {
    tenantContext
        .currentOrganization()
        .ifPresent(
            existing -> {
              throw new NotAllowedException("User already belongs to an organization");
            });
    return super.createPreProcessing(dto);
  }

  @Override
  protected <E extends OrganizationDto> Organization updatePreProcessing(
      @NotNull Long id, @NotNull E dto) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    if (!Objects.equals(callerOrg.getId(), id)) {
      throw new ResourceNotFoundException();
    }
    return super.updatePreProcessing(id, dto);
  }

  @Override
  protected void deletePreProcessing(@NotNull Long id) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    if (!Objects.equals(callerOrg.getId(), id)) {
      throw new ResourceNotFoundException();
    }
    userRepository.detachUsersFromOrganization(id);
    super.deletePreProcessing(id);
  }

  @Override
  protected <E extends OrganizationDto> Organization convertDtoToEntity(
      E dto, Optional<Organization> currentEntityOpt) {
    return Organization.builder().id(dto.getId()).name(dto.getName()).slug(dto.getSlug()).build();
  }
}
