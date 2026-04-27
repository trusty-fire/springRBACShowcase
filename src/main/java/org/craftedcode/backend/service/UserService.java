package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.Role;
import de.frachtwerk.essencium.backend.model.dto.EssenciumUserDetails;
import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import de.frachtwerk.essencium.backend.service.*;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.AppUserDto;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.UserRepository;
import org.craftedcode.backend.repository.specification.TenantSpecifications;
import org.craftedcode.backend.security.TenantContext;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService
    extends AbstractUserService<User, EssenciumUserDetails<Long>, Long, AppUserDto> {

  private final OrganizationRepository organizationRepository;
  private final TenantContext tenantContext;

  protected UserService(
      @NotNull UserRepository userRepository,
      @NotNull PasswordEncoder passwordEncoder,
      @NotNull UserMailService userMailService,
      @NotNull RoleService roleService,
      @NotNull AdminRightRoleCache adminRightRoleCache,
      @NotNull JwtTokenService jwtTokenService,
      OrganizationRepository organizationRepository,
      TenantContext tenantContext) {
    super(
        userRepository,
        passwordEncoder,
        userMailService,
        roleService,
        adminRightRoleCache,
        jwtTokenService);
    this.organizationRepository = organizationRepository;
    this.tenantContext = tenantContext;
  }

  @Override
  protected Specification<User> specificationPreProcessing(Specification<User> spec) {
    Organization org = tenantContext.requireCurrentOrganization();
    Specification<User> tenantSpec = TenantSpecifications.userBelongsToOrg(org);
    return spec == null ? tenantSpec : tenantSpec.and(spec);
  }

  @Override
  protected <E extends AppUserDto> User createPreProcessing(@NotNull E dto) {
    // System bootstrap (Essencium DefaultUserInitializer) runs without an auth context.
    // skip org enforcement when there is no authenticated principal.
    tenantContext.currentOrganization().ifPresent(org -> dto.setOrganizationId(org.getId()));
    return super.createPreProcessing(dto);
  }

  @Override
  protected <E extends AppUserDto> User updatePreProcessing(@NotNull Long id, @NotNull E dto) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    User existing = repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    if (existing.getOrganization() == null
        || !Objects.equals(callerOrg.getId(), existing.getOrganization().getId())) {
      throw new ResourceNotFoundException();
    }
    if (dto.getOrganizationId() != null
        && !Objects.equals(callerOrg.getId(), dto.getOrganizationId())) {
      throw new ResourceNotFoundException();
    }
    // Preserve the org when the DTO omits organizationId to avoid nullifying the FK on save.
    if (dto.getOrganizationId() == null) {
      dto.setOrganizationId(callerOrg.getId());
    }
    return super.updatePreProcessing(id, dto);
  }

  @Override
  protected void deletePreProcessing(@NotNull Long id) {
    Organization callerOrg = tenantContext.requireCurrentOrganization();
    User existing = repository.findById(id).orElseThrow(ResourceNotFoundException::new);
    if (existing.getOrganization() == null
        || !Objects.equals(callerOrg.getId(), existing.getOrganization().getId())) {
      throw new ResourceNotFoundException();
    }
    super.deletePreProcessing(id);
  }

  @Override
  protected @NotNull <E extends AppUserDto> User convertDtoToEntity(
      @NotNull E entity, Optional<User> currentEntityOpt) {
    Set<Role> roles =
        entity.getRoles().stream().map(roleService::getByName).collect(Collectors.toSet());
    return User.builder()
        .email(entity.getEmail())
        .enabled(entity.isEnabled())
        .roles(roles)
        .firstName(entity.getFirstName())
        .lastName(entity.getLastName())
        .locale(entity.getLocale())
        .source(entity.getSource())
        .id(entity.getId())
        .loginDisabled(entity.isLoginDisabled())
        .organization(
            entity.getOrganizationId() == null
                ? null
                : organizationRepository
                    .findById(entity.getOrganizationId())
                    .orElseThrow(ResourceNotFoundException::new))
        .build();
  }

  @Override
  public AppUserDto getNewUser() {
    return new AppUserDto();
  }
}
