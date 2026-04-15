package com.library.lms.service;

import com.library.lms.dto.BorrowingDTO;
import com.library.lms.dto.DashboardStats;
import com.library.lms.model.Borrowing;
import com.library.lms.model.User;
import com.library.lms.repository.BookRepository;
import com.library.lms.repository.BorrowingRepository;
import com.library.lms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    private final BookRepository bookRepository;
    private final BorrowingRepository borrowingRepository;
    private final UserRepository userRepository;

    public DashboardStats getDashboardStats(String username, Set<String> roles) {
        boolean managerAccess = roles.contains("ADMIN") || roles.contains("LIBRARIAN");
        return managerAccess ? buildManagerStats() : buildUserStats(username);
    }

    private DashboardStats buildManagerStats() {
        long totalBooks = bookRepository.count();
        long totalMembers = userRepository.count();
        long activeMembers = userRepository.findAll().stream().filter(User::isEnabled).count();
        long totalBorrowings = borrowingRepository.count();
        long activeBorrowings = borrowingRepository.countByStatus(Borrowing.BorrowingStatus.BORROWED);
        long overdueBorrowings = borrowingRepository.findOverdue(LocalDate.now()).size();
        long returnedBorrowings = borrowingRepository.countByStatus(Borrowing.BorrowingStatus.RETURNED);
        long availableBooks = bookRepository.countAvailable();

        List<BorrowingDTO> recent = borrowingRepository.findAllWithDetails().stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .limit(5)
                .map(BorrowingDTO::fromEntity)
                .collect(Collectors.toList());

        List<Map<String, Object>> recentList = recent.stream().map(b -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("bookTitle", b.getBookTitle());
            map.put("username", b.getUsername());
            map.put("borrowDate", b.getBorrowDate());
            map.put("status", b.getStatus());
            map.put("quantity", b.getQuantity());
            return map;
        }).collect(Collectors.toList());

        List<Map<String, Object>> topCategories = buildTopCategories(totalBooks);

        return DashboardStats.builder()
                .totalBooks(totalBooks)
                .totalMembers(totalMembers)
                .activeMembers(activeMembers)
                .totalBorrowings(totalBorrowings)
                .activeBorrowings(activeBorrowings)
                .overdueBorrowings(overdueBorrowings)
                .returnedBorrowings(returnedBorrowings)
                .availableBooks(availableBooks)
                .recentBorrowings(recentList)
                .topCategories(topCategories)
                .build();
    }

    private DashboardStats buildUserStats(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found: " + username));

        List<BorrowingDTO> myBorrowings = borrowingRepository.findAllWithDetailsByUserId(user.getId()).stream()
                .sorted((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()))
                .map(BorrowingDTO::fromEntity)
                .collect(Collectors.toList());

        long totalBooks = bookRepository.count();
        long totalBorrowings = myBorrowings.size();
        long activeBorrowings = myBorrowings.stream().filter(b -> b.getStatus() == Borrowing.BorrowingStatus.BORROWED).count();
        long overdueBorrowings = myBorrowings.stream()
                .filter(b -> b.getStatus() == Borrowing.BorrowingStatus.BORROWED && b.getDueDate().isBefore(LocalDate.now()))
                .count();
        long returnedBorrowings = myBorrowings.stream().filter(b -> b.getStatus() == Borrowing.BorrowingStatus.RETURNED).count();
        long availableBooks = bookRepository.countAvailable();

        List<Map<String, Object>> recentList = myBorrowings.stream()
                .limit(5)
                .map(b -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", b.getId());
                    map.put("bookTitle", b.getBookTitle());
                    map.put("username", b.getUsername());
                    map.put("borrowDate", b.getBorrowDate());
                    map.put("status", b.getStatus());
                    map.put("quantity", b.getQuantity());
                    return map;
                }).collect(Collectors.toList());

        List<Map<String, Object>> topCategories = buildTopCategories(totalBooks);

        return DashboardStats.builder()
                .totalBooks(totalBooks)
                .totalMembers(1L)
                .activeMembers(activeBorrowings)
                .totalBorrowings(totalBorrowings)
                .activeBorrowings(activeBorrowings)
                .overdueBorrowings(overdueBorrowings)
                .returnedBorrowings(returnedBorrowings)
                .availableBooks(availableBooks)
                .recentBorrowings(recentList)
                .topCategories(topCategories)
                .build();
    }

    private List<Map<String, Object>> buildTopCategories(long totalBooks) {
        List<String> categories = bookRepository.findAllCategories();
        return categories.stream().map(cat -> {
            Map<String, Object> map = new HashMap<>();
            map.put("name", cat);
            map.put("count", bookRepository.findByCategory(cat).size());
            return map;
        }).sorted((a, b) -> ((Integer) b.get("count")).compareTo((Integer) a.get("count")))
          .limit(5)
          .collect(Collectors.toList());
    }
}
