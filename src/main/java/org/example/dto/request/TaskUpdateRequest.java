package org.example.dto.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import org.example.model.enums.TaskStatus;
import java.time.LocalDate;
@Getter @Setter
public class TaskUpdateRequest {
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    private TaskStatus status;
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
