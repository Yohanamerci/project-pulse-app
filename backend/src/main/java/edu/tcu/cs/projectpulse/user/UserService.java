package edu.tcu.cs.projectpulse.user;

import edu.tcu.cs.projectpulse.user.dto.CreateUserRequest;
import edu.tcu.cs.projectpulse.user.dto.UpdateUserRequest;
import edu.tcu.cs.projectpulse.user.dto.UserUpdateMeRequest;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserDto> findAll() {
        return userRepository.findAll().stream().map(UserDto::from).toList();
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(UserDto::from)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    public UserDto createUser(CreateUserRequest req) {
        if (userRepository.existsByUsername(req.username()))
            throw new IllegalStateException("Username already taken: " + req.username());
        if (userRepository.existsByEmail(req.email()))
            throw new IllegalStateException("Email already in use: " + req.email());
        User user = new User();
        user.setUsername(req.username());
        user.setEmail(req.email());
        user.setPassword(passwordEncoder.encode(req.password()));
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setRole(req.role());
        user.setEnabled(true);
        return UserDto.from(userRepository.save(user));
    }

    public UserDto updateUser(Long id, UpdateUserRequest req) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        if (req.firstName() != null) user.setFirstName(req.firstName());
        if (req.lastName() != null) user.setLastName(req.lastName());
        if (req.email() != null) user.setEmail(req.email());
        if (req.enabled() != null) user.setEnabled(req.enabled());
        return UserDto.from(userRepository.save(user));
    }

    public UserDto updateMe(String username, UserUpdateMeRequest req) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + username));
        user.setFirstName(req.firstName());
        user.setLastName(req.lastName());
        user.setEmail(req.email());
        return UserDto.from(userRepository.save(user));
    }

    /** Soft-delete: sets enabled=false instead of removing the record. */
    public void disableUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
        user.setEnabled(false);
        userRepository.save(user);
    }
}
