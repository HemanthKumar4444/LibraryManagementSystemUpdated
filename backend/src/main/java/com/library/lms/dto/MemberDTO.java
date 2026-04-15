package com.library.lms.dto;

import com.library.lms.model.User;
import lombok.*;

import java.time.LocalDateTime;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {

    private Long id;
    private String username;
    private String email;
    private Set<String> roles;
    private String primaryRole;
    private String status;
    private Integer activeBorrowings;
    private Integer totalBorrowings;
    private Boolean hasOutstandingFines;
    private LocalDateTime createdAt;

    public static MemberDTO fromUser(User user, long activeBorrowings, long totalBorrowings, boolean hasOutstandingFines) {
        String primaryRole = user.getRoles() == null || user.getRoles().isEmpty()
                ? "USER"
                : user.getRoles().stream().sorted().findFirst().orElse("USER");

        return MemberDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .roles(user.getRoles())
                .primaryRole(primaryRole)
                .status(user.isEnabled() ? "ACTIVE" : "DISABLED")
                .activeBorrowings((int) activeBorrowings)
                .totalBorrowings((int) totalBorrowings)
                .hasOutstandingFines(hasOutstandingFines)
                .createdAt(user.getCreatedAt())
                .build();
    }
}
