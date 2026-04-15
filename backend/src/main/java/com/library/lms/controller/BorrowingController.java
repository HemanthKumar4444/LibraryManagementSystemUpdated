package com.library.lms.controller;

import com.library.lms.dto.BorrowRequest;
import com.library.lms.dto.BorrowingDTO;
import com.library.lms.dto.UpdateDueDateRequest;
import com.library.lms.service.BorrowingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/borrowings")
@RequiredArgsConstructor
public class BorrowingController {

    private final BorrowingService borrowingService;

    @GetMapping
    public ResponseEntity<List<BorrowingDTO>> getBorrowings(Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        boolean managerAccess = borrowingService.hasManagerRole(roles);
        if (managerAccess) {
            return ResponseEntity.ok(borrowingService.getAllBorrowings());
        }
        return ResponseEntity.ok(borrowingService.getBorrowingsForUser(authentication.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BorrowingDTO> getBorrowingById(@PathVariable Long id, Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                borrowingService.getBorrowingById(id, authentication.getName(), borrowingService.hasManagerRole(roles))
        );
    }

    @GetMapping("/overdue")
    public ResponseEntity<List<BorrowingDTO>> getOverdueBorrowings(Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                borrowingService.getOverdueBorrowings(authentication.getName(), borrowingService.hasManagerRole(roles))
        );
    }

    @PostMapping("/borrow")
    public ResponseEntity<BorrowingDTO> borrowBook(@Valid @RequestBody BorrowRequest request, Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(borrowingService.borrowBook(request, authentication.getName()));
    }

    @PostMapping("/{id}/return")
    public ResponseEntity<BorrowingDTO> returnBook(@PathVariable Long id, Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                borrowingService.returnBook(id, authentication.getName(), borrowingService.hasManagerRole(roles))
        );
    }

    @PostMapping("/{id}/renew")
    public ResponseEntity<BorrowingDTO> renewBorrowing(@PathVariable Long id, Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                borrowingService.renewBorrowing(id, authentication.getName(), borrowingService.hasManagerRole(roles))
        );
    }

    @PutMapping("/{id}/due-date")
    public ResponseEntity<BorrowingDTO> updateDueDate(@PathVariable Long id,
                                                      @Valid @RequestBody UpdateDueDateRequest request,
                                                      Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                borrowingService.updateDueDate(id, request.getDueDate(), authentication.getName(), borrowingService.hasManagerRole(roles))
        );
    }
    @PostMapping("/{id}/pay-fine")
    public ResponseEntity<BorrowingDTO> payFine(@PathVariable Long id, Authentication authentication) {
        Set<String> roles = authentication.getAuthorities().stream()
                .map(a -> a.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toSet());

        return ResponseEntity.ok(
                borrowingService.payFine(id, authentication.getName(), borrowingService.hasManagerRole(roles))
        );
    }

}
