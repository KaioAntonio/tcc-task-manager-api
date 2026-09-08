package org.example.repository;
import org.example.model.Task;
import org.example.model.enums.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;
public interface TaskRepository extends JpaRepository<Task, Long> {
    @Query("SELECT t FROM Task t WHERE t.owner.id = :userId AND t.deleted = false")
    Page<Task> findByOwnerIdAndDeletedFalse(@Param("userId") Long userId, Pageable pageable);
    @Query("SELECT t FROM Task t WHERE t.deleted = false")
    Page<Task> findAllNotDeleted(Pageable pageable);
    @Query("SELECT t FROM Task t WHERE t.owner.id = :userId AND t.status = :status AND t.deleted = false")
    Page<Task> findByOwnerIdAndStatus(@Param("userId") Long userId, @Param("status") TaskStatus status, Pageable pageable);
    @Query("SELECT t FROM Task t WHERE t.deleted = false AND t.status = :status")
    Page<Task> findByStatus(@Param("status") TaskStatus status, Pageable pageable);
    Optional<Task> findByIdAndDeletedFalse(Long id);
}
