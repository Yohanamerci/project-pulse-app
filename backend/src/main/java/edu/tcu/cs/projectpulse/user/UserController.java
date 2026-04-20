package edu.tcu.cs.projectpulse.user;

import edu.tcu.cs.projectpulse.shared.Result;
import edu.tcu.cs.projectpulse.user.dto.CreateUserRequest;
import edu.tcu.cs.projectpulse.user.dto.UpdateUserRequest;
import jakarta.validation.Valid;
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

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<List<UserDto>> getAllUsers() {
        return Result.success(userService.findAll());
    }

    @GetMapping("/{id}")
    public Result<UserDto> getUserById(@PathVariable Long id) {
        return Result.success(userService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDto> createUser(@Valid @RequestBody CreateUserRequest req) {
        return Result.success("User created", userService.createUser(req));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<UserDto> updateUser(@PathVariable Long id, @RequestBody UpdateUserRequest req) {
        return Result.success("User updated", userService.updateUser(id, req));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> disableUser(@PathVariable Long id) {
        userService.disableUser(id);
        return Result.success("User disabled", null);
    }
}
