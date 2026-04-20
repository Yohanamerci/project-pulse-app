package edu.tcu.cs.projectpulse.team.dto;

import edu.tcu.cs.projectpulse.user.User;

public record TeamMemberDto(
        Long id,
        String username,
        String firstName,
        String lastName,
        String role
) {
    public static TeamMemberDto from(User u) {
        return new TeamMemberDto(
                u.getId(),
                u.getUsername(),
                u.getFirstName(),
                u.getLastName(),
                u.getRole().name()
        );
    }
}
