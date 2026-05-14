package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.exception.NotAllowedException;
import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.dto.OrganizationDto;
import org.craftedcode.backend.model.representation.OrganizationRepresentation;
import org.craftedcode.backend.model.representation.assembler.OrganizationAssembler;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.ProjectRepository;
import org.craftedcode.backend.repository.UserRepository;
import org.craftedcode.backend.repository.specification.TenantSpecifications;
import org.craftedcode.backend.security.TenantContext;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

@Service
public class OrganizationService
    extends AbstractAssemblingEntityService<
        Organization, OrganizationDto, OrganizationRepresentation> {

  private final TenantContext tenantContext;
  private final ProjectRepository projectRepository;
  private final UserRepository userRepository;
  private final OrganizationAssembler organizationAssembler;

  protected OrganizationService(
      OrganizationRepository repository,
      OrganizationAssembler assembler,
      TenantContext tenantContext,
      ProjectRepository projectRepository,
      UserRepository userRepository) {
    super(repository, assembler);
    this.tenantContext = tenantContext;
    this.projectRepository = projectRepository;
    this.userRepository = userRepository;
    this.organizationAssembler = assembler;
  }

  @Override
  public OrganizationRepresentation toOutput(Organization entity) {
    return enricher(List.of(entity.getId())).apply(entity);
  }

  @Override
  public Page<OrganizationRepresentation> toOutput(Page<Organization> page) {
    if (page == null) {
      return null;
    }
    return page.map(enricher(page.getContent().stream().map(Organization::getId).toList()));
  }

  @Override
  public List<OrganizationRepresentation> toOutput(List<Organization> entities) {
    Function<Organization, OrganizationRepresentation> enricher =
        enricher(entities.stream().map(Organization::getId).toList());
    return entities.stream().map(enricher).toList();
  }

  /** Batch-loads project and user ids for the given organizations, avoiding N+1 queries. */
  private Function<Organization, OrganizationRepresentation> enricher(List<Long> orgIds) {
    Map<Long, List<Long>> projectIds =
        groupByParent(projectRepository.findIdsByOrganizationIds(orgIds));
    Map<Long, List<Long>> userIds = groupByParent(userRepository.findIdsByOrganizationIds(orgIds));
    return org ->
        organizationAssembler.toRepresentation(
            org,
            projectIds.getOrDefault(org.getId(), List.of()),
            userIds.getOrDefault(org.getId(), List.of()));
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
