package org.example.model;
import jakarta.persistence.*;
import lombok.*;
import org.example.model.enums.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Entity
@Table(name = "tasks")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Task {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, length = 150)
    private String title;
    @Column(length = 500)
    private String description;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TaskStatus status;
    @Column(name = "due_date")
    private LocalDate dueDate;
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column(name = "deleted", nullable = false)
    private boolean deleted = false;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        if (this.status == null) this.status = TaskStatus.PENDING;
    }
    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
