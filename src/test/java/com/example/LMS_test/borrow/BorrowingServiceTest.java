package com.example.LMS_test.borrow;

import com.example.LMS_test.ban.BanRecord;
import com.example.LMS_test.ban.BanRepository;
import com.example.LMS_test.book.Book;
import com.example.LMS_test.book.BookRepository;
import com.example.LMS_test.book.BookStatus;
import com.example.LMS_test.patron.Patron;
import com.example.LMS_test.patron.PatronRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BorrowingServiceTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private PatronRepository patronRepository;

    @Mock
    private BorrowingRecordRepository borrowingRecordRepository;

    @Mock
    private BanRepository banRepository;

    @InjectMocks
    private BorrowingService borrowingService;

    private Book book;
    private Patron patron;
    private BorrowingRecord borrowingRecord;

    @BeforeEach
    void setUp() {
        book = new Book();
        book.setId(1L);
        book.setTitle("The Great Gatsby");
        book.setBookStatus(BookStatus.AVAILABLE);

        patron = new Patron();
        patron.setId(1L);
        patron.setName("John Doe");
        patron.setContactInfo("1234");

        borrowingRecord = new BorrowingRecord();
        borrowingRecord.setId(1L);
        borrowingRecord.setBook(book);
        borrowingRecord.setPatron(patron);
        borrowingRecord.setBorrowDate(LocalDate.now());
        borrowingRecord.setReturnDate(LocalDate.now().plusMonths(3));
    }

    @Test
    void borrow_Success() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(borrowingRecord);

        // Act
        BorrowingRecord result = borrowingService.borrow(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getBook()).isEqualTo(book);
        assertThat(result.getPatron()).isEqualTo(patron);
        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now().plusMonths(3));

        verify(bookRepository, times(1)).findById(1L);
        verify(patronRepository, times(1)).findById(1L);
        verify(borrowingRecordRepository, times(1)).save(any(BorrowingRecord.class));
    }

    @Test
    void borrow_BookNotFound() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> borrowingService.borrow(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book not found")
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

        verify(bookRepository, times(1)).findById(1L);
        verify(patronRepository, never()).findById(anyLong());
    }

    @Test
    void borrow_PatronNotFound() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(patronRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> borrowingService.borrow(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patron not found")
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

        verify(bookRepository, times(1)).findById(1L);
        verify(patronRepository, times(1)).findById(1L);
    }

    @Test
    void borrow_BookUnavailable() {
        // Arrange
        book.setBookStatus(BookStatus.BORROWED);
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));

        // Act & Assert
        assertThatThrownBy(() -> borrowingService.borrow(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book is currently borrowed")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(bookRepository, times(1)).findById(1L);
        verify(patronRepository, never()).findById(anyLong());
    }

    @Test
    void borrow_PatronBanned() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(banRepository.findByPatronAndBanUntilAfter(patron, LocalDate.now()))
                .thenReturn(Optional.of(new BanRecord()));

        // Act & Assert
        assertThatThrownBy(() -> borrowingService.borrow(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("You are banned from borrowing until")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(bookRepository, times(1)).findById(1L);
        verify(patronRepository, times(1)).findById(1L);
        verify(banRepository, times(1)).findByPatronAndBanUntilAfter(patron, LocalDate.now());
    }

    @Test
    void returnBook_Success() {
        // Arrange
        when(borrowingRecordRepository.findFirstByPatron_IdAndBook_IdAndReturnDateIsNullOrderByBorrowDateDesc(1L, 1L))
                .thenReturn(Optional.of(borrowingRecord));
        when(bookRepository.save(any(Book.class))).thenReturn(book);
        when(borrowingRecordRepository.save(any(BorrowingRecord.class))).thenReturn(borrowingRecord);

        // Act
        BorrowingRecord result = borrowingService.returnBook(1L, 1L);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getReturnDate()).isEqualTo(LocalDate.now());
        assertThat(result.getBook().getBookStatus()).isEqualTo(BookStatus.AVAILABLE);

        verify(borrowingRecordRepository, times(1))
                .findFirstByPatron_IdAndBook_IdAndReturnDateIsNullOrderByBorrowDateDesc(1L, 1L);
        verify(bookRepository, times(1)).save(book);
        verify(borrowingRecordRepository, times(1)).save(borrowingRecord);
    }

    @Test
    void returnBook_NoActiveBorrowing() {
        // Arrange
        when(borrowingRecordRepository.findFirstByPatron_IdAndBook_IdAndReturnDateIsNullOrderByBorrowDateDesc(1L, 1L))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> borrowingService.returnBook(1L, 1L))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("No active borrowing record found")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(borrowingRecordRepository, times(1))
                .findFirstByPatron_IdAndBook_IdAndReturnDateIsNullOrderByBorrowDateDesc(1L, 1L);
        verify(bookRepository, never()).save(any(Book.class));
        verify(borrowingRecordRepository, never()).save(any(BorrowingRecord.class));
    }
}