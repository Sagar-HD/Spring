package com.sagar.library.repository;

import com.sagar.library.constant.BorrowStatus;
import com.sagar.library.entity.BorrowRecord;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface BorrowRecordRepository extends JpaRepository<BorrowRecord,Long> {
  boolean existsByBorrowedByIdAndBorrowedBookIdAndBorrowStatusIn(Long userId, Long bookId, Collection<BorrowStatus> borrowStatus);
  List<BorrowRecord> getAllByBorrowedById(Long borrowedById);
    List<BorrowRecord> getAllByBorrowedById(Long borrowedById,Sort sort);
    List<BorrowRecord> getAllByBorrowStatus(BorrowStatus borrowStatus, Sort sort);
    List<BorrowRecord> getAllByBorrowStatus(BorrowStatus borrowStatus);

    List<BorrowRecord> getAllByBorrowedByIdAndBorrowStatus(Long authenticatedUserId, BorrowStatus borrowStatus, Sort sort);

    List<BorrowRecord> getAllByBorrowedByIdAndBorrowStatus(Long authenticatedUserId, BorrowStatus borrowStatus);
}
