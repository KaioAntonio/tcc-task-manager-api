package org.example.service;
import lombok.RequiredArgsConstructor;
import org.example.dto.request.TaskRequest;
import org.example.dto.request.TaskUpdateRequest;
import org.example.dto.response.PagedResponse;
import org.example.dto.response.TaskResponse;
import org.example.exception.ForbiddenException;
import org.example.exception.ResourceNotFoundException;
import org.example.model.Task;
import org.example.model.User;
import org.example.model.enums.Role;
import org.example.model.enums.TaskStatus;
import org.example.repository.TaskRepository;
import org.example.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TaskService {
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;
    public PagedResponse<TaskResponse> listTasks(int page, int size, TaskStatus status, Authentication auth) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        User currentUser = getCurrentUser(auth);
        boolean isAdmin = isAdmin(auth);
        Page<Task> tasks;
        if (isAdmin) {
            tasks = (status != null)
                    ? taskRepository.findByStatus(status, pageable)
                    : taskRepository.findAllNotDeleted(pageable);
        } else {
            tasks = (status != null)
                    ? taskRepository.findByOwnerIdAndStatus(currentUser.getId(), status, pageable)
                    : taskRepository.findByOwnerIdAndDeletedFalse(currentUser.getId(), pageable);
        }
        return toPagedResponse(tasks);
    }
    public TaskResponse getTask(Long id, Authentication auth) {
        Task task = findActiveTask(id);
        validateOwnershipOrAdmin(task, auth);
        return toResponse(task);
    }
    @Transactional
    public TaskResponse createTask(TaskRequest request, Authentication auth) {
        User owner = getCurrentUser(auth);
        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .dueDate(request.getDueDate())
                .status(TaskStatus.PENDING)
                .owner(owner)
                .build();
        return toResponse(taskRepository.save(task));
    }
    @Transactional
    public TaskResponse updateTask(Long id, TaskUpdateRequest request, Authentication auth) {
        Task task = findActiveTask(id);
        validateOwnershipOrAdmin(task, auth);
        if (request.getTitle() != null) task.setTitle(request.getTitle());
        if (request.getDescription() != null) task.setDescription(request.getDescription());
        if (request.getStatus() != null) task.setStatus(request.getStatus());
        if (request.getDueDate() != null) task.setDueDate(request.getDueDate());
        return toResponse(taskRepository.save(task));
    }
    @Transactional
    public void deleteTask(Long id, Authentication auth) {
        Task task = findActiveTask(id);
        validateOwnershipOrAdmin(task, auth);
        task.setDeleted(true);
        taskRepository.save(task);
    }
    // -- v2: soft-delete + restore (evolucao do contrato) ----------------------
    @Transactional
    public TaskResponse softDelete(Long id, Authentication auth) {
        Task task = findActiveTask(id);
        validateOwnershipOrAdmin(task, auth);
        task.setDeleted(true);
        task.setStatus(TaskStatus.CANCELLED);
        return toResponse(taskRepository.save(task));
    }
    // -- Helpers ---------------------------------------------------------------
    private Task findActiveTask(Long id) {
        return taskRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task", id));
    }
    private void validateOwnershipOrAdmin(Task task, Authentication auth) {
        if (!isAdmin(auth)) {
            User currentUser = getCurrentUser(auth);
            if (!task.getOwner().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You do not have access to this task");
            }
        }
    }
    private User getCurrentUser(Authentication auth) {
        return userRepository.findByEmail(auth.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User", -1L));
    }
    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().contains(new SimpleGrantedAuthority(Role.ROLE_ADMIN.name()));
    }
    private TaskResponse toResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus())
                .dueDate(task.getDueDate())
                .ownerId(task.getOwner().getId())
                .ownerName(task.getOwner().getName())
                .createdAt(task.getCreatedAt())
                .updatedAt(task.getUpdatedAt())
                .build();
    }
    private PagedResponse<TaskResponse> toPagedResponse(Page<Task> page) {
        return PagedResponse.<TaskResponse>builder()
                .content(page.getContent().stream().map(this::toResponse).toList())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .last(page.isLast())
                .build();
    }
}
