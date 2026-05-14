package org.craftedcode.backend.repository.specification;

import de.frachtwerk.essencium.backend.repository.specification.BaseModelSpec;
import net.kaczmarzyk.spring.data.jpa.domain.In;
import net.kaczmarzyk.spring.data.jpa.domain.LessThanOrEqual;
import net.kaczmarzyk.spring.data.jpa.web.annotation.Spec;
import org.craftedcode.backend.model.Task;

@Spec(path = "status", params = "status", spec = In.class, paramSeparator = ',')
interface TaskStatusSpecification extends BaseModelSpec<Task, Long> {}

@Spec(path = "priority", params = "priority", spec = In.class, paramSeparator = ',')
interface TaskPrioritySpecification extends BaseModelSpec<Task, Long> {}

@Spec(path = "dueDate", params = "dueUntil", spec = LessThanOrEqual.class, paramSeparator = ',')
interface TaskDueDateSpecification extends BaseModelSpec<Task, Long> {}

@Spec(path = "project.id", params = "projectId", spec = In.class, paramSeparator = ',')
interface TaskProjectSpecification extends BaseModelSpec<Task, Long> {}

public interface TaskSpecification
    extends TaskStatusSpecification,
        TaskPrioritySpecification,
        TaskDueDateSpecification,
        TaskProjectSpecification {}
