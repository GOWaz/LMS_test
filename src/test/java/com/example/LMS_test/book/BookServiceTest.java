package com.example.LMS_test.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookServiceTest {

    @Mock
    private BookRepository bookRepository;

    @InjectMocks
    private BookService bookService;

    private BookRequest bookRequest;
    private Book book;

    @BeforeEach
    void setUp() {
        bookRequest = new BookRequest("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", LocalDate.of(1925, 4, 10));
        book = new Book("The Great Gatsby", "F. Scott Fitzgerald", "9780743273565", LocalDate.of(1925, 4, 10), BookStatus.AVAILABLE, null);
        book.setId(1L);
    }

    @Test
    void create_Success() {
        // Arrange
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.create(bookRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("The Great Gatsby");
        assertThat(result.getAuthor()).isEqualTo("F. Scott Fitzgerald");
        assertThat(result.getIsbn()).isEqualTo("9780743273565");
        assertThat(result.getPublishDate()).isEqualTo(LocalDate.of(1925, 4, 10));
        assertThat(result.getBookStatus()).isEqualTo(BookStatus.AVAILABLE);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void create_PublishDateInFuture() {
        // Arrange
        bookRequest.setPublishDate(LocalDate.now().plusDays(1));

        // Act & Assert
        assertThatThrownBy(() -> bookService.create(bookRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Publish date cannot be in the future")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void create_DataIntegrityViolationException() {
        // Arrange
        when(bookRepository.save(any(Book.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        // Act & Assert
        assertThatThrownBy(() -> bookService.create(bookRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book with this ISBN already exists or A book with the same title and author already exists.")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void create_UnexpectedError() {
        // Arrange
        when(bookRepository.save(any(Book.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThatThrownBy(() -> bookService.create(bookRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("An unexpected error occurred")
                .extracting("status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void update_Success() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class))).thenReturn(book);

        // Act
        Book result = bookService.update(1L, bookRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getTitle()).isEqualTo("The Great Gatsby");
        assertThat(result.getAuthor()).isEqualTo("F. Scott Fitzgerald");
        assertThat(result.getIsbn()).isEqualTo("9780743273565");
        assertThat(result.getPublishDate()).isEqualTo(LocalDate.of(1925, 4, 10));

        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void update_BookNotFound() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> bookService.update(1L, bookRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Book not found")
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void update_DataIntegrityViolationException() {
        // Arrange
        when(bookRepository.findById(1L)).thenReturn(Optional.of(book));
        when(bookRepository.save(any(Book.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        // Act & Assert
        assertThatThrownBy(() -> bookService.update(1L, bookRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("A book with the same title and author already exists.")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(bookRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }
}