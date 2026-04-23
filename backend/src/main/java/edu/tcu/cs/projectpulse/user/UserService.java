package edu.tcu.cs.projectpulse.user;

import edu.tcu.cs.projectpulse.activity.ActivityRepository;
import edu.tcu.cs.projectpulse.evaluation.PeerEvaluationRepository;
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
    private final ActivityRepository activityRepository;
    private final PeerEvaluationRepository peerEvaluationRepository;

    public UserService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       ActivityRepository activityRepository,
                       PeerEvaluationRepository peerEvaluationRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.activityRepository = activityRepository;
        this.peerEvaluationRepository = peerEvaluationRepository;
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

    public UserDto updateUser(Long id, UpdateUserRequest req, String callerUsername) {
        User caller = userRepository.findByUsername(callerUsername)
                .orElseThrow(() -> new EntityNotFoundException("Caller not found: " + callerUsername));
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found: " + id));
        // Instructors may only edit student accounts
        if (caller.getRole() == Role.INSTRUCTOR && user.getRole() != Role.STUDENT) {
            throw new IllegalStateException("Instructors can only modify student accounts.");
        }
        if (req.firstName() != null) user.setFirstName(req.firstName());
        if (req.lastName() != null) user.setLastName(req.lastName());
        if (req.email() != null) user.setEmail(req.email());
        // Only admins can change account enabled status
        if (req.enabled() != null && caller.getRole() == Role.ADMIN) {
            user.setEnabled(req.enabled());
        }
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

    /**
     * UC-17: Student deletion is a physical delete — permanently removes the student
     * and all associated WARs and peer evaluations (cascade).
     * UC-23: Instructor/Admin deactivation is a soft-delete (sets enabled=false).
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (user.getRole() == Role.STUDENT) {
            // Physical delete: cascade WAR activities and peer evaluations first
            activityRepository.deleteByStudentId(id);
            peerEvaluationRepository.deleteByEvaluatorIdOrEvaluateeId(id, id);
            // Team membership cascades automatically (FK ON DELETE CASCADE in DB)
            userRepository.deleteById(id);
        } else {
            // Soft-deactivate instructors and admins (UC-23)
            user.setEnabled(false);
            userRepository.save(user);
        }
    }
}
