package org.example.controller.v1;
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
@RestController("v1AuthController")
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth v1", description = "Autenticacao e registro de usuarios")
public class AuthController {
    private final AuthService authService;
    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar novo usuario", description = "Cria conta com role USER e retorna token JWT")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }
    @PostMapping("/login")
    @Operation(summary = "Autenticar usuario", description = "Retorna token JWT para uso nos endpoints protegidos")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
