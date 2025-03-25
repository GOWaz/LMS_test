package com.example.LMS_test.book;

import com.example.LMS_test.baseEntity.BaseEntity;
import com.example.LMS_test.borrow.BorrowingRecord;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "book", uniqueConstraints = {
        @UniqueConstraint(name = "title_author_unique", columnNames = {"title", "author", "isbn"})
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Book extends BaseEntity {

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String author;

    @Column(length = 13)
    private String isbn;

    private LocalDate publishDate;

    @Enumerated(EnumType.STRING)
    private BookStatus bookStatus;

    @OneToMany(mappedBy = "book", fetch = FetchType.LAZY)
    @JsonManagedReference
    private List<BorrowingRecord> borrowingRecords;
}
