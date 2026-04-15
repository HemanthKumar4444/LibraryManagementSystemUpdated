package com.library.lms.repository;

import com.library.lms.model.Borrowing;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface BorrowingRepository extends JpaRepository<Borrowing, Long> {

    List<Borrowing> findByUserId(Long userId);

    List<Borrowing> findByBookId(Long bookId);

    List<Borrowing> findByStatus(Borrowing.BorrowingStatus status);

    @Query("SELECT b FROM Borrowing b WHERE b.dueDate < :today AND b.status = 'BORROWED'")
    List<Borrowing> findOverdue(@Param("today") LocalDate today);

    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.status = :status")
    Long countByStatus(@Param("status") Borrowing.BorrowingStatus status);

    @Query("SELECT COUNT(b) FROM Borrowing b WHERE b.user.id = :userId AND b.status = 'BORROWED'")
    Long countActiveBorrowingsByUser(@Param("userId") Long userId);

    @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.status = 'BORROWED'")
    List<Borrowing> findActiveBorrowingsByUser(@Param("userId") Long userId);

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book JOIN FETCH b.user ORDER BY b.borrowDate DESC, b.createdAt DESC")
    List<Borrowing> findAllWithDetails();

    @Query("SELECT b FROM Borrowing b JOIN FETCH b.book JOIN FETCH b.user WHERE b.user.id = :userId ORDER BY b.borrowDate DESC, b.createdAt DESC")
    List<Borrowing> findAllWithDetailsByUserId(@Param("userId") Long userId);

    @Query("SELECT b FROM Borrowing b WHERE b.borrowDate >= :startDate AND b.borrowDate <= :endDate")
    List<Borrowing> findByBorrowDateBetween(@Param("startDate") LocalDate startDate,
                                            @Param("endDate") LocalDate endDate);

    @Query("SELECT b FROM Borrowing b WHERE b.user.id = :userId AND b.fineAmount > 0 AND (b.finePaid = false OR b.finePaid IS NULL)")
    List<Borrowing> findUnpaidFinesByUserId(@Param("userId") Long userId);
}
