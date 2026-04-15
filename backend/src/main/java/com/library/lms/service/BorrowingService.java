package com.library.lms.service;

import com.library.lms.dto.BorrowRequest;
import com.library.lms.dto.BorrowingDTO;
import com.library.lms.model.Book;
import com.library.lms.model.Borrowing;
import com.library.lms.model.User;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.BorrowingRepository;
import com.library.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BorrowingService {

    private final BorrowingRepository borrowingRepository;
    private final BookRepository bookRepository;
    private final UserRepository userRepository;

    private static final int DEFAULT_LOAN_DAYS = 7;
    private static final int MAX_ACTIVE_BORROWINGS_PER_USER = 5;
    private static final BigDecimal FINE_PER_DAY = new BigDecimal("0.50");

    public List<BorrowingDTO> getBorrowingsForUser(String username) {
        User user = getUserByUsername(username);
        return borrowingRepository.findAllWithDetailsByUserId(user.getId()).stream()
                .map(BorrowingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public List<BorrowingDTO> getAllBorrowings() {
        List<Borrowing> borrowings = borrowingRepository.findAllWithDetails();
        borrowings.forEach(this::updateOverdueFineIfNeeded);
        return borrowings.stream()
                .map(BorrowingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public BorrowingDTO getBorrowingById(Long id, String username, boolean managerAccess) {
        Borrowing borrowing = borrowingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found with id: " + id));

        updateOverdueFineIfNeeded(borrowing);

        if (!managerAccess && !borrowing.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only view your own borrowings.");
        }
        return BorrowingDTO.fromEntity(borrowing);
    }

    public List<BorrowingDTO> getOverdueBorrowings(String username, boolean managerAccess) {
        List<Borrowing> overdue = managerAccess
                ? borrowingRepository.findOverdue(LocalDate.now())
                : borrowingRepository.findAllWithDetailsByUserId(getUserByUsername(username).getId()).stream()
                .filter(b -> (b.getStatus() == Borrowing.BorrowingStatus.BORROWED || b.getStatus() == Borrowing.BorrowingStatus.OVERDUE)
                        && b.getDueDate().isBefore(LocalDate.now()))
                .collect(Collectors.toList());

        overdue.forEach(this::updateOverdueFineIfNeeded);

        return overdue.stream()
                .sorted((a, b) -> b.getBorrowDate().compareTo(a.getBorrowDate()))
                .map(BorrowingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public BorrowingDTO borrowBook(BorrowRequest request, String username) {
        Book book = bookRepository.findById(request.getBookId())
                .orElseThrow(() -> new RuntimeException("Book not found with id: " + request.getBookId()));
        User user = getUserByUsername(username);

        if (!user.isEnabled()) {
            throw new RuntimeException("Your account is disabled and cannot borrow books.");
        }

        List<Borrowing> unpaidFines = borrowingRepository.findUnpaidFinesByUserId(user.getId());
        unpaidFines.forEach(this::updateOverdueFineIfNeeded);
        if (!unpaidFines.isEmpty()) {
            BigDecimal totalUnpaid = unpaidFines.stream()
                    .map(Borrowing::getFineAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            throw new RuntimeException("You have unpaid fines of $" + totalUnpaid + ". Please pay the fine before borrowing a new book.");
        }

        int quantity = request.getQuantity() == null ? 1 : request.getQuantity();
        if (quantity < 1) {
            throw new RuntimeException("Quantity must be at least 1.");
        }
        if (book.getAvailableCopies() < quantity) {
            throw new RuntimeException("Only " + book.getAvailableCopies() + " copies of '" + book.getTitle() + "' are available.");
        }

        long activeCount = borrowingRepository.countActiveBorrowingsByUser(user.getId());
        if (activeCount >= MAX_ACTIVE_BORROWINGS_PER_USER) {
            throw new RuntimeException("You have reached the maximum limit of " + MAX_ACTIVE_BORROWINGS_PER_USER + " active borrowings.");
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate = borrowDate.plusDays(DEFAULT_LOAN_DAYS);

        Borrowing borrowing = Borrowing.builder()
                .book(book)
                .user(user)
                .quantity(quantity)
                .borrowDate(borrowDate)
                .dueDate(dueDate)
                .status(Borrowing.BorrowingStatus.BORROWED)
                .fineAmount(BigDecimal.ZERO)
                .finePaid(false)
                .notes(request.getNotes())
                .build();

        book.setAvailableCopies(book.getAvailableCopies() - quantity);
        book.setStatus(book.getAvailableCopies() > 0 ? Book.BookStatus.AVAILABLE : Book.BookStatus.UNAVAILABLE);
        bookRepository.save(book);

        return BorrowingDTO.fromEntity(borrowingRepository.save(borrowing));
    }

    public BorrowingDTO returnBook(Long borrowingId, String username, boolean managerAccess) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found with id: " + borrowingId));

        if (!managerAccess && !borrowing.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only return your own borrowed books.");
        }
        if (borrowing.getStatus() == Borrowing.BorrowingStatus.RETURNED) {
            throw new RuntimeException("This borrowing has already been returned.");
        }

        LocalDate today = LocalDate.now();
        borrowing.setReturnDate(today);
        borrowing.setStatus(Borrowing.BorrowingStatus.RETURNED);

        if (today.isAfter(borrowing.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(borrowing.getDueDate(), today);
            borrowing.setFineAmount(FINE_PER_DAY.multiply(BigDecimal.valueOf(daysLate)));
            borrowing.setFinePaid(false);
        } else {
            borrowing.setFineAmount(BigDecimal.ZERO);
            borrowing.setFinePaid(true);
        }

        Book book = borrowing.getBook();
        book.setAvailableCopies(book.getAvailableCopies() + borrowing.getQuantity());
        book.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.save(book);

        return BorrowingDTO.fromEntity(borrowingRepository.save(borrowing));
    }

    public BorrowingDTO renewBorrowing(Long borrowingId, String username, boolean managerAccess) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found."));

        updateOverdueFineIfNeeded(borrowing);

        if (!managerAccess && !borrowing.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only renew your own borrowed books.");
        }
        if (borrowing.getStatus() != Borrowing.BorrowingStatus.BORROWED) {
            throw new RuntimeException("Only active borrowings can be renewed.");
        }

        borrowing.setDueDate(borrowing.getDueDate().plusDays(DEFAULT_LOAN_DAYS));
        borrowing.setStatus(Borrowing.BorrowingStatus.BORROWED);

        return BorrowingDTO.fromEntity(borrowingRepository.save(borrowing));
    }

    public BorrowingDTO updateDueDate(Long borrowingId, LocalDate newDueDate, String username, boolean managerAccess) {
        if (!managerAccess) {
            throw new RuntimeException("Only Admin or Librarian can update due date.");
        }

        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found with id: " + borrowingId));

        if (borrowing.getStatus() == Borrowing.BorrowingStatus.RETURNED) {
            throw new RuntimeException("Due date cannot be updated for a returned book.");
        }

        LocalDate today = LocalDate.now();
        LocalDate maxAllowedDate = today.plusDays(DEFAULT_LOAN_DAYS);

        if (newDueDate.isBefore(today) || newDueDate.isAfter(maxAllowedDate)) {
            throw new RuntimeException("Custom due date must be between today and today + 7 days.");
        }

        borrowing.setDueDate(newDueDate);
        borrowing.setStatus(Borrowing.BorrowingStatus.BORROWED);
        if (!Boolean.TRUE.equals(borrowing.getFinePaid())) {
            borrowing.setFineAmount(BigDecimal.ZERO);
            borrowing.setFinePaid(false);
        }

        return BorrowingDTO.fromEntity(borrowingRepository.save(borrowing));
    }

    public BorrowingDTO payFine(Long borrowingId, String username, boolean managerAccess) {
        Borrowing borrowing = borrowingRepository.findById(borrowingId)
                .orElseThrow(() -> new RuntimeException("Borrowing record not found with id: " + borrowingId));

        updateOverdueFineIfNeeded(borrowing);

        if (!managerAccess && !borrowing.getUser().getUsername().equals(username)) {
            throw new RuntimeException("You can only pay your own fines.");
        }
        if (borrowing.getFineAmount() == null || borrowing.getFineAmount().signum() <= 0) {
            throw new RuntimeException("There is no pending fine for this borrowing.");
        }
        if (Boolean.TRUE.equals(borrowing.getFinePaid())) {
            throw new RuntimeException("This fine has already been paid.");
        }

        borrowing.setFinePaid(true);
        return BorrowingDTO.fromEntity(borrowingRepository.save(borrowing));
    }

    public long countActiveBorrowingsForUser(String username) {
        User user = getUserByUsername(username);
        return borrowingRepository.countActiveBorrowingsByUser(user.getId());
    }

    public long countReturnedBorrowingsForUser(String username) {
        User user = getUserByUsername(username);
        return borrowingRepository.findAllWithDetailsByUserId(user.getId()).stream()
                .filter(b -> b.getStatus() == Borrowing.BorrowingStatus.RETURNED)
                .count();
    }

    public long countOverdueBorrowingsForUser(String username) {
        User user = getUserByUsername(username);
        return borrowingRepository.findAllWithDetailsByUserId(user.getId()).stream()
                .peek(this::updateOverdueFineIfNeeded)
                .filter(b -> b.getStatus() == Borrowing.BorrowingStatus.OVERDUE)
                .count();
    }

    public List<BorrowingDTO> getRecentBorrowingsForUser(String username, int limit) {
        User user = getUserByUsername(username);
        return borrowingRepository.findAllWithDetailsByUserId(user.getId()).stream()
                .peek(this::updateOverdueFineIfNeeded)
                .limit(limit)
                .map(BorrowingDTO::fromEntity)
                .collect(Collectors.toList());
    }

    public boolean hasManagerRole(Set<String> roles) {
        return roles.contains("ADMIN") || roles.contains("LIBRARIAN");
    }

    private void updateOverdueFineIfNeeded(Borrowing borrowing) {
        if ((borrowing.getStatus() == Borrowing.BorrowingStatus.BORROWED || borrowing.getStatus() == Borrowing.BorrowingStatus.OVERDUE)
                && borrowing.getDueDate().isBefore(LocalDate.now())) {
            borrowing.setStatus(Borrowing.BorrowingStatus.OVERDUE);
            long daysLate = ChronoUnit.DAYS.between(borrowing.getDueDate(), LocalDate.now());
            borrowing.setFineAmount(FINE_PER_DAY.multiply(BigDecimal.valueOf(daysLate)));
            borrowing.setFinePaid(false);
            borrowingRepository.save(borrowing);
        }
    }

    private User getUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));
    }
}
