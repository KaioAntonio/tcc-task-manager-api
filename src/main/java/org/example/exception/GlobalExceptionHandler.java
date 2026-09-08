package org.example.exception;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Handler global de exceções — padroniza TODAS as respostas de erro da API.
 * Garante que nenhuma stack trace ou detalhe interno vaze para o cliente.
 * (OWASP API3:2023 - Broken Object Property Level Authorization e API9:2023)
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    // ── Erro de validação (Bean Validation) ──────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });
        return buildResponse(HttpStatus.BAD_REQUEST, "Validation failed", fieldErrors);
    }

    // ── Violação de constraint (path params, query params) ───────────────────
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException ex) {
        Map<String, String> fieldErrors = new HashMap<>();
        for (ConstraintViolation<?> v : ex.getConstraintViolations()) {
            String field = v.getPropertyPath().toString();
            fieldErrors.put(field, v.getMessage());
        }
        return buildResponse(HttpStatus.BAD_REQUEST, "Constraint violation", fieldErrors);
    }

    // ── Exceções de domínio customizadas ─────────────────────────────────────
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApiException(ApiException ex) {
        return buildResponse(ex.getStatus(), ex.getMessage(), null);
    }

    // ── Acesso negado (autorização) ───────────────────────────────────────────
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex) {
        return buildResponse(HttpStatus.FORBIDDEN, "Access denied", null);
    }

    // ── Credenciais inválidas ─────────────────────────────────────────────────
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex) {
        return buildResponse(HttpStatus.UNAUTHORIZED, "Invalid credentials", null);
    }

    // ── Tipo errado no parâmetro ──────────────────────────────────────────────
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        String message = "Invalid value '" + ex.getValue() + "' for parameter '" + ex.getName() + "'";
        return buildResponse(HttpStatus.BAD_REQUEST, message, null);
    }

    // ── Fallback geral (nunca expõe detalhes internos) ───────────────────────
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception ex) {
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", null);
    }

    // ── Builder de resposta padronizado ──────────────────────────────────────
    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message,
                                                         Map<String, String> fieldErrors) {
        ErrorResponse body = ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(status.getReasonPhrase())
                .message(message)
                .fieldErrors(fieldErrors)
                .build();
        return ResponseEntity.status(status).body(body);
    }

    // ── DTO de erro padronizado ───────────────────────────────────────────────
    @lombok.Builder
    @lombok.Getter
    public static class ErrorResponse {
        private LocalDateTime timestamp;
        private int status;
        private String error;
        private String message;
        private Map<String, String> fieldErrors;
    }
}

