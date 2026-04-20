package org.craftedcode.backend.test.integration.util;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.frachtwerk.essencium.backend.model.Role;
import de.frachtwerk.essencium.backend.model.dto.LoginRequest;
import de.frachtwerk.essencium.backend.repository.RoleRepository;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.AppUserDto;
import org.craftedcode.backend.service.UserService;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.RandomStringUtils;
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
    private final ObjectMapper objectMapper = new ObjectMapper();

    @NotNull
    public User getOrCreateAdminUser() {
        if (adminUser == null) {
            return createAdminUser();
        }
        return adminUser;
    }

    @NotNull
    public User createAdminUser() {
        adminUser = createUser("devone@frachtwerk.de", getAdminRole());
        return adminUser;
    }

    public Role getAdminRole() {
        Role role = roleRepository.findByName("ADMIN");
        if (role == null) {
            throw new RuntimeException("Required role ADMIN not found");
        }
        return role;
    }

    public User createUser(String username, Role role) {
        return createUser(username, "Test", "User", role);
    }

    public User createUser(String username, String firstName, String lastName, Role role) {
        final String sanitizedUsername =
                Objects.requireNonNullElseGet(username, TestingUtils::randomUsername);
        AppUserDto user = new AppUserDto();
        user.setEnabled(true);
        user.setEmail(sanitizedUsername);
        user.setPassword(DEFAULT_PASSWORD);
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.getRoles().add(role.getName());

        return createUser(user);
    }

    private User createUser(AppUserDto user) {
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
        return RandomStringUtils.randomAlphanumeric(5, 10) + "@frachtwerk.de";
    }

    public void clear() {
        adminUser = null;
    }
}
