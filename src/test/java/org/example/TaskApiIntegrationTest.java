package org.example;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.dto.request.LoginRequest;
import org.example.dto.request.RegisterRequest;
import org.example.dto.request.TaskRequest;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import java.time.LocalDate;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
@SpringBootTest
@AutoConfigureMockMvc
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class TaskApiIntegrationTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    private static String userToken;
    private static String adminToken;
    private static Long taskId;
    // -- Auth Tests -------------------------------------------------------------
    @Test @Order(1)
    @DisplayName("Login com admin deve retornar 200 e token JWT")
    void loginAdmin_shouldReturn200AndToken() throws Exception {
        LoginRequest req = new LoginRequest();
        req.setEmail("admin@example.com");
        req.setPassword("admin123");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andExpect(jsonPath("$.token_type").value("Bearer"))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        adminToken = objectMapper.readTree(body).get("access_token").asText();
    }
    @Test @Order(2)
    @DisplayName("Registro de novo usuario deve retornar 201")
    void registerUser_shouldReturn201() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test User");
        req.setEmail("testuser@example.com");
        req.setPassword("password123");
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.access_token").isNotEmpty())
                .andReturn();
        String body = result.getResponse().getContentAsString();
        userToken = objectMapper.readTree(body).get("access_token").asText();
    }
    @Test @Order(3)
    @DisplayName("Login sem token deve retornar 401 em endpoint protegido")
    void noToken_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/tasks"))
                .andExpect(status().isUnauthorized());
    }
    @Test @Order(4)
    @DisplayName("Registro com email invalido deve retornar 400 com fieldErrors")
    void register_withInvalidEmail_shouldReturn400() throws Exception {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test");
        req.setEmail("not-an-email");
        req.setPassword("password123");
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }
    // -- Task CRUD Tests --------------------------------------------------------
    @Test @Order(5)
    @DisplayName("Criar tarefa com token valido deve retornar 201")
    void createTask_shouldReturn201() throws Exception {
        TaskRequest req = new TaskRequest();
        req.setTitle("Estudar Spring Boot");
        req.setDescription("Implementar JWT, RBAC e OpenAPI");
        req.setDueDate(LocalDate.now().plusDays(7));
        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.title").value("Estudar Spring Boot"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andReturn();
        String body = result.getResponse().getContentAsString();
        taskId = objectMapper.readTree(body).get("id").asLong();
    }
    @Test @Order(6)
    @DisplayName("Listar tarefas proprias deve retornar pagina")
    void listTasks_shouldReturnPagedResponse() throws Exception {
        mockMvc.perform(get("/api/v1/tasks")
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.page").value(0))
                .andExpect(jsonPath("$.size").value(10));
    }
    @Test @Order(7)
    @DisplayName("Criar tarefa sem titulo deve retornar 400")
    void createTask_withoutTitle_shouldReturn400() throws Exception {
        TaskRequest req = new TaskRequest();
        req.setTitle("");
        mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.title").exists());
    }
    @Test @Order(8)
    @DisplayName("Acesso a tarefa de outro usuario deve retornar 403")
    void accessOtherUserTask_shouldReturn403() throws Exception {
        // Cria outra tarefa como admin
        TaskRequest req = new TaskRequest();
        req.setTitle("Tarefa do Admin");
        req.setDueDate(LocalDate.now().plusDays(3));
        MvcResult result = mockMvc.perform(post("/api/v1/tasks")
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        Long adminTaskId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
        // Tenta acessar como user comum -> 403
        mockMvc.perform(get("/api/v1/tasks/" + adminTaskId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }
    @Test @Order(9)
    @DisplayName("v2 DELETE deve retornar 200 + body (soft-delete)")
    void v2SoftDelete_shouldReturn200WithBody() throws Exception {
        mockMvc.perform(delete("/api/v2/tasks/" + taskId)
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }
}
