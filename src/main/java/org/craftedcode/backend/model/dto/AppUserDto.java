package org.craftedcode.backend.model.dto;

import de.frachtwerk.essencium.backend.model.dto.BaseUserDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.craftedcode.backend.model.OrgRole;

@EqualsAndHashCode(callSuper = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class AppUserDto extends BaseUserDto<Long> {

  private Long organizationId;

  private OrgRole orgRole;
}
