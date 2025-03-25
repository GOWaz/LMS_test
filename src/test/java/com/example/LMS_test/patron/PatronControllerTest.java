package com.example.LMS_test.patron;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatronControllerTest {

    @Mock
    private PatronService patronService;

    @InjectMocks
    private PatronController patronController;

    private Patron patron1;
    private Patron patron2;
    private PatronRequest patronRequest;

    @BeforeEach
    void setUp() {
        patron1 = new Patron("Patron 1", "1234", null, null);
        patron1.setId(1L);
        patron2 = new Patron("Patron 2", "5678", null, null);
        patron2.setId(2L);
        patronRequest = new PatronRequest("New Patron", "New ContactInfo");
    }

    @Test
    void getAllPatrons() {
        // Arrange
        List<Patron> patrons = Arrays.asList(patron1, patron2);
        when(patronService.getAll()).thenReturn(patrons);

        // Act
        ResponseEntity<List<Patron>> response = patronController.getAllPatrons();

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        Assertions.assertThat(response.getBody())
                .hasSize(2)
                .containsExactly(patron1, patron2);
        verify(patronService, times(1)).getAll();
    }

    @Test
    void getPatronById_ShouldReturnPatron() {
        // Arrange
        when(patronService.getById(1L)).thenReturn(patron1);

        // Act
        ResponseEntity<Patron> response = patronController.getPatronById(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(Patron::getName, Patron::getContactInfo)
                .containsExactly("Patron 1", "1234");
        verify(patronService, times(1)).getById(1L);
    }

    @Test
    void createPatron_ShouldReturnCreatedPatron() {
        // Arrange
        Patron newPatron = new Patron("New Patron", "New ContactInfo", null, null);
        newPatron.setId(3L);
        when(patronService.create(patronRequest)).thenReturn(newPatron);

        // Act
        ResponseEntity<Patron> response = patronController.createPatron(patronRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(Patron::getId, Patron::getName, Patron::getContactInfo)
                .containsExactly(3L, "New Patron", "New ContactInfo");
        verify(patronService, times(1)).create(patronRequest);
    }

    @Test
    void updatePatron_ShouldReturnUpdatedPatron() {
        // Arrange
        Patron updatedPatron = new Patron("Updated Patron", "Updated ContactInfo", null, null);
        updatedPatron.setId(4L);
        when(patronService.update(1L, patronRequest)).thenReturn(updatedPatron);

        // Act
        ResponseEntity<Patron> response = patronController.updatePatron(1L, patronRequest);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody())
                .isNotNull()
                .extracting(Patron::getName, Patron::getContactInfo)
                .containsExactly("Updated Patron", "Updated ContactInfo");
        verify(patronService, times(1)).update(1L, patronRequest);
    }

    @Test
    void deletePatron_ShouldReturnNoContent() {
        // Arrange
        doNothing().when(patronService).delete(1L);

        // Act
        ResponseEntity<Void> response = patronController.deletePatron(1L);

        // Assert
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(response.getBody()).isNull();
        verify(patronService, times(1)).delete(1L);
    }
}