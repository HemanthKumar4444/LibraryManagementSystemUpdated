package com.library.lms.repository;

import com.library.lms.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByEmail(String email);

    Optional<Member> findByMembershipId(String membershipId);

    boolean existsByEmail(String email);

    boolean existsByMembershipId(String membershipId);

    List<Member> findByStatus(Member.MemberStatus status);

    @Query("SELECT m FROM Member m WHERE " +
           "LOWER(m.firstName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.lastName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(m.membershipId) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Member> searchMembers(@Param("keyword") String keyword);

    @Query("SELECT COUNT(m) FROM Member m WHERE m.status = :status")
    Long countByStatus(@Param("status") Member.MemberStatus status);
}
