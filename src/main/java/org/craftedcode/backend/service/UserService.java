package org.craftedcode.backend.service;

import de.frachtwerk.essencium.backend.model.Role;
import de.frachtwerk.essencium.backend.model.dto.EssenciumUserDetails;
import de.frachtwerk.essencium.backend.model.exception.ResourceNotFoundException;
import de.frachtwerk.essencium.backend.service.*;
import jakarta.validation.constraints.NotNull;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.AppUserDto;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService
    extends AbstractUserService<User, EssenciumUserDetails<Long>, Long, AppUserDto> {

  private final OrganizationRepository organizationRepository;

  protected UserService(
      @NotNull UserRepository userRepository,
      @NotNull PasswordEncoder passwordEncoder,
      @NotNull UserMailService userMailService,
      @NotNull RoleService roleService,
      @NotNull AdminRightRoleCache adminRightRoleCache,
      @NotNull JwtTokenService jwtTokenService,
      OrganizationRepository organizationRepository) {
    super(
        userRepository,
        passwordEncoder,
        userMailService,
        roleService,
        adminRightRoleCache,
        jwtTokenService);
    this.organizationRepository = organizationRepository;
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
            organizationRepository
                .findById(entity.getOrganizationId())
                .orElseThrow(ResourceNotFoundException::new))
        .build();
  }

  @Override
  public AppUserDto getNewUser() {
    return new AppUserDto();
  }
}
