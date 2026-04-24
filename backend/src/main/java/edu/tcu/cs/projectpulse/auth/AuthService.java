package edu.tcu.cs.projectpulse.auth;

import edu.tcu.cs.projectpulse.auth.dto.LoginRequest;
import edu.tcu.cs.projectpulse.auth.dto.LoginResponse;
import edu.tcu.cs.projectpulse.auth.dto.RegisterRequest;
import edu.tcu.cs.projectpulse.system.NotificationService;
import edu.tcu.cs.projectpulse.user.Role;
import edu.tcu.cs.projectpulse.user.User;
import edu.tcu.cs.projectpulse.user.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;
    private final InvitationTokenRepository invitationTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final NotificationService notificationService;

    public AuthService(AuthenticationManager authManager,
                       JwtProvider jwtProvider,
                       UserRepository userRepository,
                       InvitationTokenRepository invitationTokenRepository,
                       PasswordEncoder passwordEncoder,
                       NotificationService notificationService) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
        this.invitationTokenRepository = invitationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.username())
                .orElseThrow();

        return new LoginResponse(user.getId(), user.getUsername(), user.getRole().name(), token);
    }

    /** UC-30 step 1: Admin sends invitation email to prospective instructor. */
    public void inviteInstructor(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalStateException("An account with this email already exists.");
        }
        String rawToken = UUID.randomUUID().toString();
        InvitationToken invite = new InvitationToken();
        invite.setToken(rawToken);
        invite.setEmail(email);
        invite.setExpiresAt(LocalDateTime.now().plusDays(7));
        invitationTokenRepository.save(invite);
        notificationService.sendInvitationEmail(email, rawToken);
    }

    /** UC-30 step 2: Instructor completes registration via the invitation link. */
    public LoginResponse registerInstructor(RegisterRequest req) {
        if (!req.password().equals(req.confirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match.");
        }

        InvitationToken invite = invitationTokenRepository.findByToken(req.token())
                .orElseThrow(() -> new EntityNotFoundException("Invalid or expired invitation token."));
        if (invite.isUsed()) {
            throw new IllegalStateException("This invitation has already been used.");
        }
        if (invite.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalStateException("This invitation link has expired. Ask an admin to resend it.");
        }

        // Auto-generate username: firstname.lastname (lowercase, no spaces)
        String base = (req.firstName() + "." + req.lastName()).toLowerCase().replaceAll("[^a-z0-9.]", "");
        String username = base;
        int suffix = 1;
        while (userRepository.existsByUsername(username)) {
            username = base + suffix++;
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(invite.getEmail());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFirstName(req.firstName());
        if (req.middleInitial() != null && !req.middleInitial().isBlank()) {
            user.setMiddleInitial(req.middleInitial().substring(0, 1).toUpperCase());
        }
        user.setLastName(req.lastName());
        user.setRole(Role.INSTRUCTOR);
        user.setEnabled(true);
        userRepository.save(user);

        invite.setUsed(true);
        invitationTokenRepository.save(invite);

        // Auto-login: authenticate with raw password before it's hashed on the next load
        Authentication auth = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(username, req.password())
        );
        String jwt = jwtProvider.generateToken(auth);
        return new LoginResponse(user.getId(), username, Role.INSTRUCTOR.name(), jwt);
    }
}
