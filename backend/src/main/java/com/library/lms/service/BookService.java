package com.library.lms.service;

import com.library.lms.dto.BookDTO;
import com.library.lms.model.Book;
import com.library.lms.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;

    public List<BookDTO> getAllBooks() {
        return bookRepository.findAll().stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public BookDTO getBookById(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        return BookDTO.fromEntity(book);
    }

    public BookDTO getBookByIsbn(String isbn) {
        Book book = bookRepository.findByIsbn(isbn)
                .orElseThrow(() -> new RuntimeException("Book not found with ISBN: " + isbn));
        return BookDTO.fromEntity(book);
    }

    public BookDTO createBook(BookDTO dto) {
        if (bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new RuntimeException("A book with ISBN " + dto.getIsbn() + " already exists.");
        }
        Book book = Book.builder()
                .title(dto.getTitle())
                .author(dto.getAuthor())
                .isbn(dto.getIsbn())
                .category(dto.getCategory())
                .publisher(dto.getPublisher())
                .publishedYear(dto.getPublishedYear())
                .totalCopies(dto.getTotalCopies())
                .availableCopies(dto.getTotalCopies())
                .description(dto.getDescription())
                .coverImageUrl(dto.getCoverImageUrl())
                .status(Book.BookStatus.AVAILABLE)
                .build();
        return BookDTO.fromEntity(bookRepository.save(book));
    }

    public BookDTO updateBook(Long id, BookDTO dto) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        if (!book.getIsbn().equals(dto.getIsbn()) && bookRepository.existsByIsbn(dto.getIsbn())) {
            throw new RuntimeException("A book with ISBN " + dto.getIsbn() + " already exists.");
        }

        int borrowedCopies = book.getTotalCopies() - book.getAvailableCopies();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setIsbn(dto.getIsbn());
        book.setCategory(dto.getCategory());
        book.setPublisher(dto.getPublisher());
        book.setPublishedYear(dto.getPublishedYear());
        book.setTotalCopies(dto.getTotalCopies());
        book.setAvailableCopies(Math.max(0, dto.getTotalCopies() - borrowedCopies));
        book.setDescription(dto.getDescription());
        book.setCoverImageUrl(dto.getCoverImageUrl());
        if (dto.getStatus() != null) {
            book.setStatus(dto.getStatus());
        }

        return BookDTO.fromEntity(bookRepository.save(book));
    }


    public BookDTO addSingleCopy(Long id) {
        return addCopies(id, 1);
    }

    public BookDTO addCopies(Long id, Integer copiesToAdd) {
        if (copiesToAdd == null || copiesToAdd < 1) {
            throw new RuntimeException("At least 1 copy must be added.");
        }

        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        book.setTotalCopies(book.getTotalCopies() + copiesToAdd);
        book.setAvailableCopies(book.getAvailableCopies() + copiesToAdd);
        book.setStatus(Book.BookStatus.AVAILABLE);

        return BookDTO.fromEntity(bookRepository.save(book));
    }

    public BookDTO removeSingleCopy(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));

        if (book.getTotalCopies() <= 1) {
            throw new RuntimeException("Only one copy is left. Use delete entire book to remove it.");
        }

        if (book.getAvailableCopies() <= 0) {
            throw new RuntimeException("No available copies can be removed because all copies are currently borrowed.");
        }

        book.setTotalCopies(book.getTotalCopies() - 1);
        book.setAvailableCopies(book.getAvailableCopies() - 1);

        if (book.getAvailableCopies() <= 0) {
            book.setStatus(Book.BookStatus.UNAVAILABLE);
        } else {
            book.setStatus(Book.BookStatus.AVAILABLE);
        }

        return BookDTO.fromEntity(bookRepository.save(book));
    }

    public void deleteBook(Long id) {
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + id));
        if (book.getAvailableCopies() < book.getTotalCopies()) {
            throw new RuntimeException("Cannot delete entire book while some copies are borrowed.");
        }
        bookRepository.delete(book);
    }

    public List<BookDTO> searchBooks(String keyword) {
        return bookRepository.searchBooks(keyword).stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BookDTO> getBooksByCategory(String category) {
        return bookRepository.findByCategory(category).stream()
                .map(BookDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<String> getAllCategories() {
        return bookRepository.findAllCategories();
    }
}
