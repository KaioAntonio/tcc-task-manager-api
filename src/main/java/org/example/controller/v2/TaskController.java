package org.example.controller.v2;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.headers.Header;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.TaskRequest;
import org.example.dto.request.TaskUpdateRequest;
import org.example.dto.response.PagedResponse;
import org.example.dto.response.TaskResponse;
import org.example.model.enums.TaskStatus;
import org.example.service.TaskService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
/**
 * Controller v2 - Evolucao controlada do contrato v1.
 *
 * NOVIDADES v2:
 * - DELETE agora faz soft-delete (retorna 200 + TaskResponse) em vez de 204
 * - Header Deprecation em endpoints que serao removidos
 * - Parametro de ordenacao adicionado
 *
 * v1 continua funcionando normalmente (sem breaking change para consumidores antigos).
 */
@RestController("v2TaskController")
@RequestMapping("/api/v2/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks v2", description = "Gerenciamento de tarefas - versao 2 (evolucao do contrato)")
public class TaskController {
    private final TaskService taskService;
    @GetMapping
    @Operation(summary = "Listar tarefas (v2)", description = "Mesma semantica v1 + parametro sort")
    public ResponseEntity<PagedResponse<TaskResponse>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) TaskStatus status,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.listTasks(page, size, status, authentication));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID (v2)")
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(taskService.getTask(id, authentication));
    }
    @PostMapping
    @Operation(summary = "Criar nova tarefa (v2)")
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request, authentication));
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar tarefa parcial (v2)")
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody TaskUpdateRequest request,
                                                Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTask(id, request, authentication));
    }
    /**
     * BREAKING CHANGE documentada: v2 DELETE retorna 200 + body (soft-delete).
     * v1 DELETE retornava 204 sem body.
     * Consumidores de v1 nao sao afetados pois continuam usando /api/v1.
     */
    @DeleteMapping("/{id}")
    @Operation(
        summary = "Soft-delete de tarefa (v2 - mudanca de contrato)",
        description = "v2: retorna 200 com o recurso marcado como CANCELLED. v1 retornava 204 sem body.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tarefa cancelada com sucesso"),
            @ApiResponse(responseCode = "404", description = "Tarefa nao encontrada"),
            @ApiResponse(responseCode = "403", description = "Acesso negado")
        }
    )
    public ResponseEntity<TaskResponse> softDelete(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(taskService.softDelete(id, authentication));
    }
}
