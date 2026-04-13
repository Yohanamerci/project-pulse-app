package edu.tcu.cs.projectpulse.user;

import edu.tcu.cs.projectpulse.shared.Result;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /** GET /api/v1/users — Admin only */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserDto>> getAllUsers() {
        return Result.success(userService.findAll());
    }

    /** GET /api/v1/users/{id} — Admin, or the user themselves */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or #id == authentication.principal.subject.toLong()")
    public Result<UserDto> getUserById(@PathVariable Long id) {
        return Result.success(userService.findById(id));
    }
}
