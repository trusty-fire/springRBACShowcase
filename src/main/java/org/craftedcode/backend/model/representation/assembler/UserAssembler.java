package org.craftedcode.backend.model.representation.assembler;

import de.frachtwerk.essencium.backend.model.representation.assembler.AbstractRepresentationAssembler;
import org.craftedcode.backend.model.User;
import lombok.NonNull;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Primary
@Component
public class UserAssembler extends AbstractRepresentationAssembler<User, User> {
    @Override
    public @NonNull User toModel(@NonNull User entity) {
        return entity;
    }
}