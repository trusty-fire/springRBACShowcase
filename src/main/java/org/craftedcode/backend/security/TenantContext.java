package org.craftedcode.backend.security;

import de.frachtwerk.essencium.backend.model.dto.EssenciumUserDetails;
import de.frachtwerk.essencium.backend.model.exception.NotAllowedException;
import java.util.Optional;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Component
public class TenantContext {

  private static final String CACHE_KEY = TenantContext.class.getName() + ".currentOrg";

  private final UserRepository userRepository;

  public TenantContext(UserRepository userRepository) {
    this.userRepository = userRepository;
  }

  public Optional<Organization> currentOrganization() {
    RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
    if (attrs == null) {
      return resolveOrganization();
    }
    Cached cached = (Cached) attrs.getAttribute(CACHE_KEY, RequestAttributes.SCOPE_REQUEST);
    if (cached != null) {
      return cached.value();
    }
    Optional<Organization> resolved = resolveOrganization();
    attrs.setAttribute(CACHE_KEY, new Cached(resolved), RequestAttributes.SCOPE_REQUEST);
    return resolved;
  }

  public Organization requireCurrentOrganization() {
    return currentOrganization()
        .orElseThrow(() -> new NotAllowedException("User has no organization"));
  }

  private Optional<Organization> resolveOrganization() {
    Authentication auth = SecurityContextHolder.getContext().getAuthentication();
    if (auth == null || !(auth.getPrincipal() instanceof EssenciumUserDetails<?> details)) {
      return Optional.empty();
    }
    if (!(details.getId() instanceof Long userId)) {
      return Optional.empty();
    }
    return userRepository.findById(userId).map(User::getOrganization);
  }

  private record Cached(Optional<Organization> value) {}
}
