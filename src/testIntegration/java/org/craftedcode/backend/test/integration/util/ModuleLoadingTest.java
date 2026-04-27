package org.craftedcode.backend.test.integration.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.craftedcode.backend.controller.UserController;
import org.craftedcode.backend.model.representation.assembler.UserAssembler;
import org.craftedcode.backend.repository.UserRepository;
import org.craftedcode.backend.service.UserService;
import org.craftedcode.backend.test.integration.IntegrationTestApplication;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(
    classes = IntegrationTestApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
class ModuleLoadingTest {

  @Autowired(required = false)
  private UserController userController;

  @Autowired(required = false)
  private UserAssembler userAssembler;

  @Autowired(required = false)
  private UserRepository userRepository;

  @Autowired(required = false)
  private UserService userService;

  @Test
  void onlySessionModuleIsLoaded() {
    assertThat(userController).isNotNull();
    assertThat(userAssembler).isNotNull();
    assertThat(userRepository).isNotNull();
    assertThat(userService).isNotNull();
  }
}
