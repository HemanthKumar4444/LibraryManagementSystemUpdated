package com.library.lms.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "members")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "First name is required")
    @Column(nullable = false)
    private String firstName;

    @NotBlank(message = "Last name is required")
    @Column(nullable = false)
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email must be valid")
    @Column(unique = true, nullable = false)
    private String email;

    @Pattern(regexp = "^[0-9+\\-() ]{7,20}$", message = "Phone number is invalid")
    private String phone;

    private String address;

    @Column(unique = true, nullable = false)
    private String membershipId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipType membershipType = MembershipType.STANDARD;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MemberStatus status = MemberStatus.ACTIVE;

    private LocalDate membershipExpiry;

    @Column(nullable = false)
    private Integer activeBorrowings = 0;

    @Column(nullable = false)
    private Integer totalBorrowings = 0;

    @Column(updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (membershipId == null) {
            membershipId = "MEM-" + System.currentTimeMillis();
        }
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public enum MembershipType {
        STANDARD, PREMIUM, STUDENT, SENIOR
    }

    public enum MemberStatus {
        ACTIVE, INACTIVE, SUSPENDED
    }
}
