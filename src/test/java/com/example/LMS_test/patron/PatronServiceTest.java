package com.example.LMS_test.patron;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatronServiceTest {

    @Mock
    private PatronRepository patronRepository;

    @InjectMocks
    private PatronService patronService;

    private PatronRequest patronRequest;
    private Patron patron;

    @BeforeEach
    void setUp() {
        patronRequest = new PatronRequest("John Doe", "1234");
        patron = new Patron("John Doe", "1234", null, null);
        patron.setId(1L);
    }

    @Test
    void create_Success() {
        // Arrange
        when(patronRepository.save(any(Patron.class))).thenReturn(patron);

        // Act
        Patron result = patronService.create(patronRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getContactInfo()).isEqualTo("1234");

        verify(patronRepository, times(1)).save(any(Patron.class));
    }

    @Test
    void create_DataIntegrityViolationException() {
        // Arrange
        when(patronRepository.save(any(Patron.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        // Act & Assert
        assertThatThrownBy(() -> patronService.create(patronRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patron already exists")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(patronRepository, times(1)).save(any(Patron.class));
    }

    @Test
    void create_UnexpectedError() {
        // Arrange
        when(patronRepository.save(any(Patron.class)))
                .thenThrow(new RuntimeException("Unexpected error"));

        // Act & Assert
        assertThatThrownBy(() -> patronService.create(patronRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("An unexpected error occurred")
                .extracting("status").isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);

        verify(patronRepository, times(1)).save(any(Patron.class));
    }

    @Test
    void update_Success() {
        // Arrange
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(patronRepository.save(any(Patron.class))).thenReturn(patron);

        // Act
        Patron result = patronService.update(1L, patronRequest);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("John Doe");
        assertThat(result.getContactInfo()).isEqualTo("1234");

        verify(patronRepository, times(1)).findById(1L);
        verify(patronRepository, times(1)).save(any(Patron.class));
    }

    @Test
    void update_PatronNotFound() {
        // Arrange
        when(patronRepository.findById(1L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> patronService.update(1L, patronRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patron not found")
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

        verify(patronRepository, times(1)).findById(1L);
        verify(patronRepository, never()).save(any(Patron.class));
    }

    @Test
    void update_DataIntegrityViolationException() {
        // Arrange
        when(patronRepository.findById(1L)).thenReturn(Optional.of(patron));
        when(patronRepository.save(any(Patron.class)))
                .thenThrow(new DataIntegrityViolationException("Unique constraint violation"));

        // Act & Assert
        assertThatThrownBy(() -> patronService.update(1L, patronRequest))
                .isInstanceOf(ResponseStatusException.class)
                .hasMessageContaining("Patron already exists")
                .extracting("status").isEqualTo(HttpStatus.BAD_REQUEST);

        verify(patronRepository, times(1)).findById(1L);
        verify(patronRepository, times(1)).save(any(Patron.class));
    }
}