package com.library.lms.dto;

import com.library.lms.model.Book;
import jakarta.validation.constraints.*;
import lombok.*;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BookDTO {

    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Author is required")
    private String author;

    @NotBlank(message = "ISBN is required")
    private String isbn;

    @NotBlank(message = "Category is required")
    private String category;

    private String publisher;

    private Integer publishedYear;

    @NotNull(message = "Total copies is required")
    @Min(value = 1, message = "Must have at least 1 copy")
    private Integer totalCopies;

    private Integer availableCopies;

    private String description;

    private String coverImageUrl;

    private Book.BookStatus status;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    public static BookDTO fromEntity(Book book) {
        return BookDTO.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .isbn(book.getIsbn())
                .category(book.getCategory())
                .publisher(book.getPublisher())
                .publishedYear(book.getPublishedYear())
                .totalCopies(book.getTotalCopies())
                .availableCopies(book.getAvailableCopies())
                .description(book.getDescription())
                .coverImageUrl(book.getCoverImageUrl())
                .status(book.getStatus())
                .createdAt(book.getCreatedAt())
                .updatedAt(book.getUpdatedAt())
                .build();
    }
}
