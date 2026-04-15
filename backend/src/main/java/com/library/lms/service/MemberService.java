package com.library.lms.service;

import com.library.lms.dto.CreateUserRequest;
import com.library.lms.dto.MemberDTO;
import com.library.lms.dto.UpdateUserRequest;
import com.library.lms.model.Borrowing;
import com.library.lms.model.User;
import com.library.lms.repository.BorrowingRepository;
import com.library.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberService {

    private final UserRepository userRepository;
    private final BorrowingRepository borrowingRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<MemberDTO> getAllMembers() {
        return userRepository.findAll().stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public MemberDTO getMemberById(Long id) {
        User user = getUserById(id);
        return toDto(user);
    }

    @Transactional(readOnly = true)
    public List<MemberDTO> searchMembers(String keyword) {
        String q = keyword == null ? "" : keyword.trim().toLowerCase(Locale.ROOT);
        return userRepository.findAll().stream()
                .filter(user -> user.getUsername().toLowerCase(Locale.ROOT).contains(q)
                        || user.getEmail().toLowerCase(Locale.ROOT).contains(q)
                        || user.getRoles().stream().anyMatch(role -> role.toLowerCase(Locale.ROOT).contains(q)))
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    public MemberDTO createUser(CreateUserRequest request) {
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        Set<String> roles = normalizeRoles(request.getRoles());

        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists.");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists.");
        }

        User saved = userRepository.save(User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .roles(roles)
                .enabled(request.getEnabled() == null || request.getEnabled())
                .build());

        return toDto(saved);
    }

    public MemberDTO updateUser(Long id, UpdateUserRequest request, String currentUsername) {
        User user = getUserById(id);
        String username = request.getUsername().trim();
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        Set<String> roles = normalizeRoles(request.getRoles());

        userRepository.findByUsername(username)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new RuntimeException("Username already exists."); });

        userRepository.findByEmail(email)
                .filter(existing -> !existing.getId().equals(id))
                .ifPresent(existing -> { throw new RuntimeException("Email already exists."); });

        if (currentUsername != null && currentUsername.equals(user.getUsername()) && !roles.contains("ADMIN")) {
            throw new RuntimeException("You cannot remove ADMIN role from your own account.");
        }

        user.setUsername(username);
        user.setEmail(email);
        user.setRoles(roles);
        user.setEnabled(request.getEnabled() == null || request.getEnabled());

        return toDto(userRepository.save(user));
    }

    public void deleteUser(Long id, String currentUsername) {
        User user = getUserById(id);

        if (currentUsername != null && currentUsername.equals(user.getUsername())) {
            throw new RuntimeException("You cannot delete your own account.");
        }

        long activeBorrowings = borrowingRepository.findByUserId(user.getId()).stream()
                .filter(borrowing -> borrowing.getStatus() == Borrowing.BorrowingStatus.BORROWED
                        || borrowing.getStatus() == Borrowing.BorrowingStatus.OVERDUE)
                .count();

        if (activeBorrowings > 0) {
            throw new RuntimeException("Cannot delete user with active borrowings.");
        }

        userRepository.delete(user);
    }

    @Transactional(readOnly = true)
    private MemberDTO toDto(User user) {
        long totalBorrowings = borrowingRepository.findByUserId(user.getId()).size();
        long activeBorrowings = borrowingRepository.findByUserId(user.getId()).stream()
                .filter(borrowing -> borrowing.getStatus() == Borrowing.BorrowingStatus.BORROWED
                        || borrowing.getStatus() == Borrowing.BorrowingStatus.OVERDUE)
                .count();
        long outstandingFineCount = borrowingRepository.findByUserId(user.getId()).stream()
                .filter(borrowing -> borrowing.getFineAmount() != null
                        && borrowing.getFineAmount().signum() > 0
                        && !Boolean.TRUE.equals(borrowing.getFinePaid()))
                .count();
        return MemberDTO.fromUser(user, activeBorrowings, totalBorrowings, outstandingFineCount > 0);
    }

    private User getUserById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    private Set<String> normalizeRoles(Set<String> roles) {
        if (roles == null || roles.isEmpty()) {
            throw new RuntimeException("At least one role is required.");
        }
        Set<String> normalized = roles.stream()
                .map(role -> role == null ? "" : role.trim().toUpperCase(Locale.ROOT))
                .filter(role -> !role.isBlank())
                .collect(Collectors.toSet());

        if (normalized.isEmpty()) {
            throw new RuntimeException("At least one valid role is required.");
        }

        normalized.forEach(role -> {
            if (!Set.of("ADMIN", "LIBRARIAN", "USER").contains(role)) {
                throw new RuntimeException("Invalid role: " + role);
            }
        });

        return normalized;
    }
}
