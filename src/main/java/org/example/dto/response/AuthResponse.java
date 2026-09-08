package org.example.dto.response;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
@Getter @Builder @AllArgsConstructor
public class AuthResponse {
    @JsonProperty("access_token")
    private String accessToken;
    @JsonProperty("token_type")
    private final String tokenType = "Bearer";
    @JsonProperty("expires_in")
    private long expiresIn;
    private UserResponse user;
}
