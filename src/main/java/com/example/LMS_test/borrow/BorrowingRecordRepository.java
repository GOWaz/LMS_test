package com.example.LMS_test.borrow;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BorrowingRecordRepository extends JpaRepository<BorrowingRecord, Long> {

    Optional<BorrowingRecord> findFirstByPatron_IdAndReturnDateIsNullOrderByBorrowDateDesc(Long patronId);

    Optional<BorrowingRecord> findFirstByPatron_IdAndBook_IdAndReturnDateIsNullOrderByBorrowDateDesc(Long patronId, Long bookId);
}
