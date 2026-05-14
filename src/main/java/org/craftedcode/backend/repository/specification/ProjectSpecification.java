package org.craftedcode.backend.repository.specification;

import de.frachtwerk.essencium.backend.repository.specification.BaseModelSpec;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.craftedcode.backend.model.Project;

@Spec(path = "status", params = "status", spec = In.class, paramSeparator = ',')
interface ProjectStatusSpecification extends BaseModelSpec<Project, Long> {}

@Spec(path = "organization.id", params = "organizationId", spec = In.class, paramSeparator = ',')
interface ProjectOrganizationSpecification extends BaseModelSpec<Project, Long> {}

public interface ProjectSpecification
    extends ProjectStatusSpecification, ProjectOrganizationSpecification {}
