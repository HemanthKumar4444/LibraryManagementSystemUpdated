package com.library.lms.dto;

import com.library.lms.model.Borrowing;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BorrowingDTO {

    private Long id;
    private Long bookId;
    private String bookTitle;
    private String bookIsbn;
    private Long userId;
    private String username;
    private String userEmail;
    private Integer quantity;
    private LocalDate borrowDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Borrowing.BorrowingStatus status;
    private BigDecimal fineAmount;
    private Boolean finePaid;
    private String notes;
    private LocalDateTime createdAt;

    public static BorrowingDTO fromEntity(Borrowing borrowing) {
        return BorrowingDTO.builder()
                .id(borrowing.getId())
                .bookId(borrowing.getBook().getId())
                .bookTitle(borrowing.getBook().getTitle())
                .bookIsbn(borrowing.getBook().getIsbn())
                .userId(borrowing.getUser().getId())
                .username(borrowing.getUser().getUsername())
                .userEmail(borrowing.getUser().getEmail())
                .quantity(borrowing.getQuantity())
                .borrowDate(borrowing.getBorrowDate())
                .dueDate(borrowing.getDueDate())
                .returnDate(borrowing.getReturnDate())
                .status(borrowing.getStatus())
                .fineAmount(borrowing.getFineAmount())
                .finePaid(borrowing.getFinePaid())
                .notes(borrowing.getNotes())
                .createdAt(borrowing.getCreatedAt())
                .build();
    }
}
