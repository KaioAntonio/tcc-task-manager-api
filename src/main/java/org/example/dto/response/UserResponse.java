package org.example.dto.response;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.example.model.enums.Role;
import java.time.LocalDateTime;
@Getter @Builder @AllArgsConstructor
public class UserResponse {
    private Long id;
    private String name;
    private String email;
    private Role role;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}
