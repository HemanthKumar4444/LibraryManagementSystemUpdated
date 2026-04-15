package com.library.lms.dto;

import lombok.*;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DashboardStats {

    private Long totalBooks;
    private Long totalMembers;
    private Long activeMembers;
    private Long totalBorrowings;
    private Long activeBorrowings;
    private Long overdueBorrowings;
    private Long returnedBorrowings;
    private Long availableBooks;
    private List<Map<String, Object>> recentBorrowings;
    private List<Map<String, Object>> topCategories;
}
