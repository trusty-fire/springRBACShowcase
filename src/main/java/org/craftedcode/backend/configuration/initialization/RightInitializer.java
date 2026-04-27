package org.craftedcode.backend.configuration.initialization;

import de.frachtwerk.essencium.backend.configuration.initialization.DefaultRightInitializer;
import de.frachtwerk.essencium.backend.model.Right;
import de.frachtwerk.essencium.backend.repository.RoleRepository;
import de.frachtwerk.essencium.backend.service.RightService;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Primary
@Configuration
public class RightInitializer extends DefaultRightInitializer {

  @Autowired
  public RightInitializer(RightService rightService, RoleRepository roleRepository) {
    super(rightService, roleRepository);
  }

  @Override
  public Set<Right> getAdditionalApplicationRights() {

    List<String> models = List.of("ORGANIZATION", "PROJECT", "TASK");

    List<String> defaultCrud = List.of("CREATE", "READ", "UPDATE", "DELETE");

    return models.stream()
        .flatMap(
            model ->
                defaultCrud.stream()
                    .map(
                        crud ->
                            Right.builder()
                                .authority(String.format("%s_%s", model, crud))
                                .description(
                                    capitalize(crud.toLowerCase())
                                        + " "
                                        + capitalize(model.toLowerCase()))
                                .build()))
        .collect(Collectors.toSet());
  }

  private static String capitalize(String s) {
    return s.isEmpty() ? s : Character.toUpperCase(s.charAt(0)) + s.substring(1);
  }
}
