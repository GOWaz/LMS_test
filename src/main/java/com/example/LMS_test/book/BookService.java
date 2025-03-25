package com.example.LMS_test.book;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookService {
    private static final Logger logger = LoggerFactory.getLogger(BookService.class);
    private final BookRepository bookRepository;

    @Cacheable("books")
    public List<Book> getAll() {
        return bookRepository.findAll();
    }

    @Cacheable(value = "books", key = "#id")
    public Book getById(Long id) {
        return bookRepository.findById(id).orElseThrow(() -> {
            logger.warn("Book with id {} not found", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
        });
    }

    @CacheEvict(value = "books", allEntries = true)
    @Transactional
    public Book create(BookRequest bookRequest) {
        logger.debug("Creating new book with title: {}", bookRequest.getTitle());

        // Validate publish date
        if (bookRequest.getPublishDate().isAfter(LocalDate.now())) {
            logger.warn("Publish Date is after now");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Publish date cannot be in the future");
        }

        try {
            Book book = new Book(
                    bookRequest.getTitle(),
                    bookRequest.getAuthor(),
                    bookRequest.getIsbn(),
                    bookRequest.getPublishDate(),
                    BookStatus.AVAILABLE,
                    null);
            Book savedBook = bookRepository.save(book);
            logger.info("Created book with ID: {}", savedBook.getId());
            return savedBook;
        } catch (DataIntegrityViolationException e) {
            logger.error("Unique constraint violation while saving book", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Book with this ISBN already exists or A book with the same title and author already exists.");
        } catch (Exception e) {
            logger.error("Unexpected error while creating book", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }


    @CacheEvict(value = "books", allEntries = true)
    @Transactional
    public Book update(Long id, BookRequest bookRequest) {
        logger.debug("Updating book with ID: {}", id);
        return bookRepository.findById(id).map(book -> {
            if (!bookRequest.getTitle().isEmpty()) {
                book.setTitle(bookRequest.getTitle());
            }
            if (!bookRequest.getAuthor().isEmpty()) {
                book.setAuthor(bookRequest.getAuthor());
            }
            if (!bookRequest.getIsbn().isEmpty()) {
                book.setIsbn(bookRequest.getIsbn());
            }
            if (bookRequest.getPublishDate() != null) {
                book.setPublishDate(bookRequest.getPublishDate());
            }
            try {
                return bookRepository.save(book);
            } catch (DataIntegrityViolationException e) {
                logger.error("Unique constraint violation during update for book ID: {}", id, e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "A book with the same title and author already exists.");
            }
        }).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found"));
    }

    @CacheEvict(value = "books", allEntries = true)
    public void delete(Long id) {
        logger.debug("Deleting book with ID: {}", id);
        Book book = bookRepository.findById(id)
                .orElseThrow(() -> {
                    logger.warn("Book with ID {} not found for update", id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Book not found");
                });
        bookRepository.delete(book);
        logger.info("Successfully deleted book with ID {}, title: {}", id, book.getTitle());
    }
}
