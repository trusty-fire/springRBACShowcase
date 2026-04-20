package org.craftedcode.backend.model;

import de.frachtwerk.essencium.backend.model.IdentityIdModel;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@EqualsAndHashCode(callSuper = true)
@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
public abstract class AbstractModel extends IdentityIdModel {}
