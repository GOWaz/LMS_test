package com.example.LMS_test.ban;

import com.example.LMS_test.patron.Patron;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface BanRepository extends JpaRepository<BanRecord, Long> {
    Optional<BanRecord> findByPatronAndBanUntilAfter(Patron patron, LocalDate date);
}


