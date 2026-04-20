package org.craftedcode.backend.controller;

import de.frachtwerk.essencium.backend.controller.AbstractUserController;
import de.frachtwerk.essencium.backend.model.AbstractBaseUser;
import de.frachtwerk.essencium.backend.model.dto.BaseUserDto;
import de.frachtwerk.essencium.backend.model.dto.EssenciumUserDetails;
import de.frachtwerk.essencium.backend.repository.specification.BaseUserSpec;
import org.craftedcode.backend.model.User;
import org.craftedcode.backend.model.dto.AppUserDto;
import org.craftedcode.backend.repository.specification.UserSpecification;
import org.craftedcode.backend.service.UserService;
import org.craftedcode.backend.model.representation.assembler.UserAssembler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.Serializable;

@RestController
@RequestMapping("/v1/users")
public class UserController extends AbstractUserController<User, EssenciumUserDetails<Long>, User, AppUserDto, UserSpecification, Long> {
    protected UserController(UserService userService, UserAssembler assembler) {
        super(userService, assembler);
    }
}