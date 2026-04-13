package edu.tcu.cs.projectpulse.auth;

import edu.tcu.cs.projectpulse.auth.dto.LoginRequest;
import edu.tcu.cs.projectpulse.auth.dto.LoginResponse;
import edu.tcu.cs.projectpulse.user.User;
import edu.tcu.cs.projectpulse.user.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final AuthenticationManager authManager;
    private final JwtProvider jwtProvider;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authManager,
                       JwtProvider jwtProvider,
                       UserRepository userRepository) {
        this.authManager = authManager;
        this.jwtProvider = jwtProvider;
        this.userRepository = userRepository;
    }

    public LoginResponse login(LoginRequest request) {
        Authentication authentication = authManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        String token = jwtProvider.generateToken(authentication);

        User user = userRepository.findByUsername(request.username())
                .orElseThrow();

        return new LoginResponse(user.getId(), user.getUsername(), user.getRole().name(), token);
    }
}
