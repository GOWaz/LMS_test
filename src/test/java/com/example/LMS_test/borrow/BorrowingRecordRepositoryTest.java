package com.example.LMS_test.borrow;

import com.example.LMS_test.book.Book;
import com.example.LMS_test.book.BookStatus;
import com.example.LMS_test.patron.Patron;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@DataJpaTest
class BorrowingRecordRepositoryTest {
    @Autowired
    private BorrowingRecordRepository underTest;

    @Autowired
    private TestEntityManager entityManager;

    private Book book;
    private Patron patron;
    private BorrowingRecord borrowingRecord2;

    @BeforeEach
    void setUp() {
        patron = new Patron();
        patron.setName("Abd");
        patron.setContactInfo("1234");

        book = new Book();
        book.setTitle("Code");
        book.setAuthor("Tahseen");
        book.setIsbn("1234567891234");
        book.setPublishDate(LocalDate.now());
        book.setBookStatus(BookStatus.BORROWED);

        entityManager.persist(patron);
        entityManager.persist(book);

        BorrowingRecord borrowingRecord1 = new BorrowingRecord();
        borrowingRecord1.setBook(book);
        borrowingRecord1.setPatron(patron);
        borrowingRecord1.setBorrowDate(LocalDate.of(2024, 12, 1));
        borrowingRecord1.setReturnDate(null);

        borrowingRecord2 = new BorrowingRecord();
        borrowingRecord2.setBook(book);
        borrowingRecord2.setPatron(patron);
        borrowingRecord2.setBorrowDate(LocalDate.of(2025, 2, 1));
        borrowingRecord2.setReturnDate(null);

        entityManager.persist(borrowingRecord1);
        entityManager.persist(borrowingRecord2);

        entityManager.flush();
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findFirstByPatron_IdAndReturnDateIsNullOrderByBorrowDateDesc() {
        // Act
        Optional<BorrowingRecord> result = underTest.findFirstByPatron_IdAndReturnDateIsNullOrderByBorrowDateDesc(patron.getId());

        // Assert
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(record -> {
                    assertThat(record.getId()).isEqualTo(borrowingRecord2.getId());
                    assertThat(record.getBorrowDate()).isEqualTo(borrowingRecord2.getBorrowDate());
                });
    }

    @Test
    void findFirstByPatron_IdAndBook_IdAndReturnDateIsNullOrderByBorrowDateDesc() {
        // Act
        Optional<BorrowingRecord> result = underTest.findFirstByPatron_IdAndBook_IdAndReturnDateIsNullOrderByBorrowDateDesc(patron.getId(), book.getId());

        // Assert
        assertThat(result)
                .isPresent()
                .hasValueSatisfying(record -> {
                    assertThat(record.getId()).isEqualTo(borrowingRecord2.getId());
                    assertThat(record.getBorrowDate()).isEqualTo(borrowingRecord2.getBorrowDate());
                });
    }
}