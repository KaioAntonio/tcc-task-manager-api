package org.example.controller.v1;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
 * Controller v1 - CRUD basico de Tasks.
 * Demonstra: naming RESTful, HTTP methods corretos, status codes padronizados,
 * paginacao, filtros e autorizacao por RBAC.
 */
@RestController("v1TaskController")
@RequestMapping("/api/v1/tasks")
@RequiredArgsConstructor
@Tag(name = "Tasks v1", description = "Gerenciamento de tarefas - versao 1")
public class TaskController {
    private final TaskService taskService;
    @GetMapping
    @Operation(summary = "Listar tarefas", description = "USER ve somente suas tarefas; ADMIN ve todas. Suporta paginacao e filtro por status.",
        responses = {
            @ApiResponse(responseCode = "200", description = "Listagem paginada retornada com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invalido")
        }
    )
    public ResponseEntity<PagedResponse<TaskResponse>> list(
            @Parameter(description = "Numero da pagina (0-based)") @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamanho da pagina") @RequestParam(defaultValue = "10") int size,
            @Parameter(description = "Filtrar por status") @RequestParam(required = false) TaskStatus status,
            Authentication authentication) {
        return ResponseEntity.ok(taskService.listTasks(page, size, status, authentication));
    }
    @GetMapping("/{id}")
    @Operation(summary = "Buscar tarefa por ID",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tarefa encontrada"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invalido"),
            @ApiResponse(responseCode = "403", description = "Tarefa pertence a outro usuario"),
            @ApiResponse(responseCode = "404", description = "Tarefa nao encontrada")
        }
    )
    public ResponseEntity<TaskResponse> getById(@PathVariable Long id, Authentication authentication) {
        return ResponseEntity.ok(taskService.getTask(id, authentication));
    }
    @PostMapping
    @Operation(summary = "Criar nova tarefa",
        responses = {
            @ApiResponse(responseCode = "201", description = "Tarefa criada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos (ex.: titulo vazio)"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invalido")
        }
    )
    public ResponseEntity<TaskResponse> create(@Valid @RequestBody TaskRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED).body(taskService.createTask(request, authentication));
    }
    @PatchMapping("/{id}")
    @Operation(summary = "Atualizar tarefa (parcial)",
        responses = {
            @ApiResponse(responseCode = "200", description = "Tarefa atualizada com sucesso"),
            @ApiResponse(responseCode = "400", description = "Dados invalidos"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invalido"),
            @ApiResponse(responseCode = "403", description = "Tarefa pertence a outro usuario"),
            @ApiResponse(responseCode = "404", description = "Tarefa nao encontrada")
        }
    )
    public ResponseEntity<TaskResponse> update(@PathVariable Long id,
                                                @Valid @RequestBody TaskUpdateRequest request,
                                                Authentication authentication) {
        return ResponseEntity.ok(taskService.updateTask(id, request, authentication));
    }
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Remover tarefa (exclusao logica em v1)",
        description = "v1: marca a tarefa como excluida e retorna 204 sem corpo.",
        responses = {
            @ApiResponse(responseCode = "204", description = "Tarefa removida com sucesso"),
            @ApiResponse(responseCode = "401", description = "Token ausente ou invalido"),
            @ApiResponse(responseCode = "403", description = "Tarefa pertence a outro usuario"),
            @ApiResponse(responseCode = "404", description = "Tarefa nao encontrada")
        }
    )
    public ResponseEntity<Void> delete(@PathVariable Long id, Authentication authentication) {
        taskService.deleteTask(id, authentication);
        return ResponseEntity.noContent().build();
    }
}
