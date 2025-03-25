package com.example.LMS_test.borrow;

import com.example.LMS_test.ban.BanRecord;
import com.example.LMS_test.ban.BanRepository;
import com.example.LMS_test.book.Book;
import com.example.LMS_test.book.BookRepository;
import com.example.LMS_test.book.BookStatus;
import com.example.LMS_test.patron.Patron;
import com.example.LMS_test.patron.PatronRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BorrowingService {
    private static final Logger logger = LoggerFactory.getLogger(BorrowingService.class);

    private static final int LOAN_PERIOD_MONTHS = 3;
    private static final int GRACE_PERIOD_WEEKS = 1;
    private static final int BAN_PERIOD_MONTHS = 6;

    private final BookRepository bookRepository;
    private final PatronRepository patronRepository;
    private final BorrowingRecordRepository borrowingRecordRepository;
    private final BanRepository banRepository;

    @Transactional
    public BorrowingRecord borrow(Long bookId, Long patronId) {
        logger.debug("Starting borrow process for book ID: {} and patron ID: {}", bookId, patronId);

        try {
            Book book = findAndValidateBook(bookId);
            Patron patron = findAndValidatePatron(patronId);
            validatePatronEligibility(patron);

            BorrowingRecord borrowingRecord = createBorrowingRecord(book, patron);
            logger.info("Successfully created borrowing record: Book ID: {}, Patron ID: {}, Due Date: {}",
                    bookId, patronId, borrowingRecord.getReturnDate());

            return borrowingRecord;
        } catch (Exception e) {
            logger.error("Error in borrow process: Book ID: {}, Patron ID: {}", bookId, patronId, e);
            throw e;
        }
    }

    private Book findAndValidateBook(Long bookId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> {
                    logger.warn("Book not found: {}", bookId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
                });

        if (book.getBookStatus() != BookStatus.AVAILABLE) {
            logger.warn("Book unavailable: {}, current status: {}", bookId, book.getBookStatus());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book is currently borrowed");
        }

        return book;
    }

    private Patron findAndValidatePatron(Long patronId) {
        return patronRepository.findById(patronId)
                .orElseThrow(() -> {
                    logger.warn("Patron not found: {}", patronId);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Patron not found");
                });
    }

    private void validatePatronEligibility(Patron patron) {
        logger.debug("Validating patron eligibility: {}", patron.getId());
        checkForUnreturnedBooks(patron);
        checkForActiveBan(patron);
    }

    private void checkForUnreturnedBooks(Patron patron) {
        borrowingRecordRepository
                .findFirstByPatron_IdAndReturnDateIsNullOrderByBorrowDateDesc(patron.getId())
                .ifPresent(lastBorrowing -> {
                    LocalDate dueDate = lastBorrowing.getBorrowDate().plusMonths(LOAN_PERIOD_MONTHS);
                    LocalDate gracePeriodEnd = dueDate.plusWeeks(GRACE_PERIOD_WEEKS);

                    if (LocalDate.now().isAfter(gracePeriodEnd)) {
                        handleOverdueBan(patron);
                    } else {
                        logger.warn("Patron has unreturned book: {}", patron.getId());
                        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                                "You must return your previous book before borrowing again.");
                    }
                });
    }

    private void handleOverdueBan(Patron patron) {
        LocalDate banUntil = LocalDate.now().plusMonths(BAN_PERIOD_MONTHS);
        BanRecord banRecord = new BanRecord(banUntil, patron);
        banRepository.save(banRecord);
        logger.warn("Patron banned for overdue book: {}, ban until: {}", patron.getId(), banUntil);
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                "You are banned from borrowing until " + banUntil);
    }

    @Transactional
    public BorrowingRecord returnBook(Long bookId, Long patronId) {
        logger.debug("Processing book return: Book ID: {}, Patron ID: {}", bookId, patronId);

        try {
            BorrowingRecord borrowingRecord = findActiveBorrowing(bookId, patronId);

            checkAndHandleLateFees(borrowingRecord);
            return processBookReturn(borrowingRecord);
        } catch (Exception e) {
            logger.error("Error processing book return: Book ID: {}, Patron ID: {}", bookId, patronId, e);
            throw e;
        }
    }

    private void checkForActiveBan(Patron patron) {
        Optional<BanRecord> activeBan = banRepository.findByPatronAndBanUntilAfter(patron, LocalDate.now());
        if (activeBan.isPresent()) {
            logger.warn("Patron has active ban: {}, ban until: {}", patron.getId(), activeBan.get().getBanUntil());
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "You are banned from borrowing until " + activeBan.get().getBanUntil());
        }
    }

    private BorrowingRecord createBorrowingRecord(Book book, Patron patron) {
        BorrowingRecord borrowingRecord = new BorrowingRecord();
        borrowingRecord.setBook(book);
        borrowingRecord.setPatron(patron);
        borrowingRecord.setBorrowDate(LocalDate.now());

        book.setBookStatus(BookStatus.BORROWED);
        bookRepository.save(book);

        return borrowingRecordRepository.save(borrowingRecord);
    }

    private BorrowingRecord findActiveBorrowing(Long bookId, Long patronId) {
        return borrowingRecordRepository
                .findFirstByPatron_IdAndBook_IdAndReturnDateIsNullOrderByBorrowDateDesc(patronId, bookId)
                .orElseThrow(() -> {
                    logger.warn("No active borrowing found: Book ID: {}, Patron ID: {}", bookId, patronId);
                    return new ResponseStatusException(HttpStatus.BAD_REQUEST, "No active borrowing record found");
                });
    }

    private void checkAndHandleLateFees(BorrowingRecord borrowingRecord) {
        LocalDate dueDate = borrowingRecord.getBorrowDate().plusMonths(LOAN_PERIOD_MONTHS);
        LocalDate gracePeriodEnd = dueDate.plusWeeks(GRACE_PERIOD_WEEKS);

        if (LocalDate.now().isAfter(gracePeriodEnd)) {
            LocalDate banUntil = LocalDate.now().plusMonths(BAN_PERIOD_MONTHS);
            BanRecord banRecord = new BanRecord(banUntil, borrowingRecord.getPatron());
            banRepository.save(banRecord);
            logger.warn("Patron banned for late return: {}, ban until: {}",
                    borrowingRecord.getPatron().getId(), banUntil);
        }
    }

    private BorrowingRecord processBookReturn(BorrowingRecord borrowingRecord) {
        borrowingRecord.setReturnDate(LocalDate.now());

        Book book = borrowingRecord.getBook();
        book.setBookStatus(BookStatus.AVAILABLE);
        bookRepository.save(book);

        logger.info("Book returned successfully: Book ID: {}, Patron ID: {}",
                book.getId(), borrowingRecord.getPatron().getId());
        return borrowingRecordRepository.save(borrowingRecord);
    }
}