package org.example.controller.v2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.LoginRequest;
import org.example.dto.request.RegisterRequest;
import org.example.dto.response.AuthResponse;
import org.example.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController("v2AuthController")
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
@Tag(name = "Auth v2", description = "Autenticacao - versao 2")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    @Operation(summary = "Registrar usuario (v2)")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
    @PostMapping("/login")
    @Operation(summary = "Login (v2)")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
