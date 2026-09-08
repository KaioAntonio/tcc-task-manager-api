package org.example.dto.request;
import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDate;
@Getter @Setter
public class TaskRequest {
    @NotBlank(message = "Title is required")
    @Size(min = 3, max = 150, message = "Title must be between 3 and 150 characters")
    private String title;
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;
    @FutureOrPresent(message = "Due date must be today or in the future")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate dueDate;
}
