package org.example.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.model.enums.TaskStatus;
import java.time.LocalDate;
import java.time.LocalDateTime;
@Getter @Builder @AllArgsConstructor
public class TaskResponse {
    private Long id;
    private String title;
    private String description;
    private TaskStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
    private Long ownerId;
    private String ownerName;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;
}
