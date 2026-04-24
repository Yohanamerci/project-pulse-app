package edu.tcu.cs.projectpulse.user;

public record UserDto(
        Long id,
        String username,
        String email,
        String firstName,
        String middleInitial,
        String lastName,
        String role,
        boolean enabled
) {
    public static UserDto from(User user) {
        return new UserDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFirstName(),
                user.getMiddleInitial(),
                user.getLastName(),
                user.getRole().name(),
                user.isEnabled()
        );
    }
}
