package edu.tcu.cs.projectpulse.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.tcu.cs.projectpulse.auth.dto.LoginRequest;
import edu.tcu.cs.projectpulse.auth.dto.LoginResponse;
import edu.tcu.cs.projectpulse.shared.Result;
import edu.tcu.cs.projectpulse.shared.StatusCode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import edu.tcu.cs.projectpulse.shared.GlobalExceptionHandler;
import edu.tcu.cs.projectpulse.system.SecurityConfig;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
class AuthControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    AuthService authService;

    @Test
    void login_withValidCredentials_returns200AndToken() throws Exception {
        LoginRequest request = new LoginRequest("admin", "Admin1234!");
        LoginResponse response = new LoginResponse(1L, "admin", "ADMIN", "mock-jwt-token");

        given(authService.login(any(LoginRequest.class))).willReturn(response);

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.flag").value(true))
                .andExpect(jsonPath("$.code").value(StatusCode.SUCCESS))
                .andExpect(jsonPath("$.data.username").value("admin"))
                .andExpect(jsonPath("$.data.role").value("ADMIN"))
                .andExpect(jsonPath("$.data.token").value("mock-jwt-token"));
    }

    @Test
    void login_withBadCredentials_returns401() throws Exception {
        LoginRequest request = new LoginRequest("admin", "wrong-password");

        given(authService.login(any(LoginRequest.class)))
                .willThrow(new BadCredentialsException("Bad credentials"));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.flag").value(false))
                .andExpect(jsonPath("$.code").value(StatusCode.UNAUTHORIZED));
    }

    @Test
    void login_withMissingUsername_returns400() throws Exception {
        String body = """
                { "username": "", "password": "Admin1234!" }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false));
    }

    @Test
    void login_withMissingPassword_returns400() throws Exception {
        String body = """
                { "username": "admin", "password": "" }
                """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.flag").value(false));
    }
}
