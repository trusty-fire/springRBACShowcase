package org.craftedcode.backend.test.integration.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.frachtwerk.essencium.backend.model.Role;
import de.frachtwerk.essencium.backend.model.dto.LoginRequest;
import de.frachtwerk.essencium.backend.repository.RoleRepository;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
import org.craftedcode.backend.model.Organization;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.AppUserDto;
import org.craftedcode.backend.repository.OrganizationRepository;
import org.craftedcode.backend.service.UserService;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

@Component
@RequiredArgsConstructor
public class TestingUtils {
  public static final String DEFAULT_PASSWORD = "password";

  private User adminUser = null;
  private final RoleRepository roleRepository;
  private final UserService userService;
  private final OrganizationRepository organizationRepository;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @NotNull
  public User createAdminUser(Organization org) {
    adminUser = createUserInOrg("devone@crafted-code.org", getAdminRole(), org);
    return adminUser;
  }

  public Role getAdminRole() {
    return getRoleByName("ADMIN");
  }

  public Role getRoleByName(String name) {
    Role role = roleRepository.findByName(name);
    if (role == null) {
      throw new RuntimeException("Required role " + name + " not found");
    }
    return role;
  }

  public Organization createOrganization(String name, String slug) {
    return organizationRepository.save(Organization.builder().name(name).slug(slug).build());
  }

  public User createUserInOrg(String email, Role role, Organization org) {
    final String sanitizedEmail =
        Objects.requireNonNullElseGet(email, TestingUtils::randomUsername);
    AppUserDto user = new AppUserDto();
    user.setEnabled(true);
    user.setEmail(sanitizedEmail);
    user.setPassword(DEFAULT_PASSWORD);
    user.setFirstName("Test");
    user.setLastName("User");
    user.getRoles().add(role.getName());
    user.setOrganizationId(org.getId());
    return userService.create(user);
  }

  public String createAccessToken(User executingUser, MockMvc mockMvc) throws Exception {
    LoginRequest loginRequest = new LoginRequest(executingUser.getEmail(), DEFAULT_PASSWORD);
    String loginRequestJson = objectMapper.writeValueAsString(loginRequest);

    ResultActions result =
        mockMvc
            .perform(
                post("/auth/token")
                    .header("user-agent", "JUnit")
                    .content(loginRequestJson)
                    .contentType(MediaType.APPLICATION_JSON_VALUE)
                    .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE));

    String resultString = result.andReturn().getResponse().getContentAsString();
    JsonNode responseJson = objectMapper.readTree(resultString);
    return responseJson.get("token").asText();
  }

  private static String randomUsername() {
    return RandomStringUtils.secureStrong().nextAlphanumeric(5, 10) + "@crafted-code.org";
  }

  public void clear() {
    adminUser = null;
  }
}
