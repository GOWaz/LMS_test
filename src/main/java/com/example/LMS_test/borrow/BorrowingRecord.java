package com.example.LMS_test.borrow;

import com.example.LMS_test.baseEntity.BaseEntity;
import com.example.LMS_test.book.Book;
import com.example.LMS_test.patron.Patron;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class BorrowingRecord extends BaseEntity {

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "book_id", nullable = false)
    @JsonBackReference
    private Book book;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "patron_id", nullable = false)
    @JsonBackReference
    private Patron patron;

    @Column(
            nullable = false,
            updatable = false
    )
    private LocalDate borrowDate;

    private LocalDate returnDate;
}


