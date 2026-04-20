package org.craftedcode.backend.model.dto;

import de.frachtwerk.essencium.backend.model.dto.BaseUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
public class AppUserDto extends BaseUserDto<Long> {}