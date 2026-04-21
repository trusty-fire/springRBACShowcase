package org.craftedcode.backend.model.representation.assembler;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.craftedcode.backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserAssemblerTest {

  @InjectMocks UserAssembler SUT;

  @Test
  void toModel() {
    User user =
        User.builder().id(1L).firstName("test").lastName("user").email("test@example.com").build();

    var model = SUT.toModel(user);

    assertThat(model).isNotNull();
    assertThat(model).isInstanceOf(User.class);
    assertEquals(user, model);
  }
}
