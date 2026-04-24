package edu.tcu.cs.projectpulse.auth;

import edu.tcu.cs.projectpulse.auth.dto.InviteRequest;
import edu.tcu.cs.projectpulse.auth.dto.LoginRequest;
import edu.tcu.cs.projectpulse.auth.dto.LoginResponse;
import edu.tcu.cs.projectpulse.auth.dto.RegisterRequest;
import edu.tcu.cs.projectpulse.shared.Result;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /** POST /api/v1/auth/login — returns JWT token + user info */
    @PostMapping("/login")
    public Result<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return Result.success("Login successful", authService.login(request));
    }

    /**
     * POST /api/v1/auth/invite — Admin sends an invitation email to a prospective instructor.
     * Body: { "email": "instructor@example.com" }
     */
    @PostMapping("/invite")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> inviteInstructor(@Valid @RequestBody InviteRequest req) {
        authService.inviteInstructor(req.email());
        return Result.success("Invitation sent to " + req.email(), null);
    }

    /**
     * POST /api/v1/auth/register — Instructor completes self-registration via invitation link.
     * Body: { "token": "...", "firstName": "...", "middleInitial": "...", "lastName": "...",
     *         "password": "...", "confirmPassword": "..." }
     * Returns: JWT token + user info (auto-login on success)
     */
    @PostMapping("/register")
    public Result<LoginResponse> registerInstructor(@Valid @RequestBody RegisterRequest req) {
        return Result.success("Account created successfully", authService.registerInstructor(req));
    }
}
