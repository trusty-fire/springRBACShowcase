package org.craftedcode.backend.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
public class TaskAssignment {

  @Id
  @ManyToOne
  @JoinColumn(name = "task_id")
  private Task task;

  @Id
  @ManyToOne
  @JoinColumn(name = "user_id")
  private User user;

  @Column(name = "assigned_at")
  private LocalDateTime assignedAt;
}
