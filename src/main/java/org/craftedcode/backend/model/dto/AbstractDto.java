package org.craftedcode.backend.model.dto;

import de.frachtwerk.essencium.backend.model.Identifiable;
import jakarta.annotation.Nullable;
import jakarta.persistence.MappedSuperclass;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Data
@SuperBuilder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@MappedSuperclass
public abstract class AbstractDto implements Identifiable<Long> {
    @Nullable private Long id;
}
