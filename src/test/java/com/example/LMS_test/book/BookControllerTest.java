package com.example.LMS_test.book;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookControllerTest {

    @Mock
    private BookService bookService;

    @InjectMocks
    private BookController bookController;

    private Book book1;
    private Book book2;
    private BookRequest bookRequest;

    @BeforeEach
    void setUp() {
        book1 = new Book("Book 1", "Author 1", "ISBN-1:1234567", LocalDate.now(), null, null);
        book1.setId(1L);
        book2 = new Book("Book 1", "Author 2", "ISBN-2:1234567", LocalDate.now(), null, null);
        book2.setId(2L);
        bookRequest = new BookRequest("New Book", "New Author", "ISBN-NEW", LocalDate.now());
    }

    @Test
    void getAllBooks_ShouldReturnAllBooks() {
        // Arrange
        List<Book> books = Arrays.asList(book1, book2);
        when(bookService.getAll()).thenReturn(books);

        // Act
        ResponseEntity<List<Book>> response = bookController.getAllBooks();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .hasSize(2)
                .containsExactly(book1, book2);
        verify(bookService, times(1)).getAll();
    }

    @Test
    void getBookById_ShouldReturnBook() {
        // Arrange
        when(bookService.getById(1L)).thenReturn(book1);

        // Act
        ResponseEntity<Book> response = bookController.getBookById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(Book::getTitle, Book::getAuthor)
                .containsExactly("Book 1", "Author 1");
        verify(bookService, times(1)).getById(1L);
    }

    @Test
    void createBook_ShouldReturnCreatedBook() {
        // Arrange
        Book newBook = new Book("New Book", "New Author", "ISBN-3:1234567", LocalDate.now(), null, null);
        newBook.setId(3L);
        when(bookService.create(bookRequest)).thenReturn(newBook);

        // Act
        ResponseEntity<Book> response = bookController.createBook(bookRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(
                        Book::getId,
                        Book::getTitle,
                        Book::getAuthor,
                        Book::getIsbn,
                        Book::getPublishDate
                )
                .containsExactly(3L, "New Book", "New Author", "ISBN-3:1234567", LocalDate.now());
        verify(bookService, times(1)).create(bookRequest);
    }

    @Test
    void updateBook_ShouldReturnUpdatedBook() {
        // Arrange
        Book updatedBook = new Book("Updated Book", "Updated Author", "ISBN-3:1234567", LocalDate.now(), null, null);
        updatedBook.setId(3L);
        when(bookService.update(1L, bookRequest)).thenReturn(updatedBook);

        // Act
        ResponseEntity<Book> response = bookController.updateBook(1L, bookRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(Book::getTitle, Book::getAuthor)
                .containsExactly("Updated Book", "Updated Author");
        verify(bookService, times(1)).update(1L, bookRequest);
    }

    @Test
    void deleteBook_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(bookService).delete(1L);

        // Act
        ResponseEntity<Void> response = bookController.deleteBook(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(bookService, times(1)).delete(1L);
    }
}