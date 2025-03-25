package com.example.LMS_test.ban;

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
class BanRepositoryTest {

    @Autowired
    private BanRepository underTest;

    @Autowired
    private TestEntityManager entityManager;

    private Patron patron1;
    private Patron patron2;
    private BanRecord ban3;


    @BeforeEach
    void setUp() {
        patron1 = new Patron();
        patron1.setName("Abd");
        patron1.setContactInfo("1234");

        patron2 = new Patron();
        patron2.setName("Tahseen");
        patron2.setContactInfo("12345");

        entityManager.persist(patron1);
        entityManager.persist(patron2);

        BanRecord ban1 = new BanRecord();
        ban1.setPatron(patron1);
        ban1.setBanUntil(LocalDate.of(2024, 1, 1));

        BanRecord ban2 = new BanRecord();
        ban2.setPatron(patron2);
        ban2.setBanUntil(LocalDate.of(2020, 1, 1));

        ban3 = new BanRecord();
        ban3.setPatron(patron1);
        ban3.setBanUntil(LocalDate.now().plusMonths(3));

        entityManager.persist(ban1);
        entityManager.persist(ban2);
        entityManager.persist(ban3);

        entityManager.flush();
    }

    @AfterEach
    void tearDown() {
        underTest.deleteAll();
    }

    @Test
    void findByPatronAndBanUntilAfter() {
        // Act
        Optional<BanRecord> result = underTest.findByPatronAndBanUntilAfter(patron1, LocalDate.now());

        // Assert
        assertThat(result).isPresent().hasValueSatisfying(record -> {
            assertThat(record.getId()).isEqualTo(ban3.getId());
            assertThat(record.getPatron().getName()).isEqualTo(ban3.getPatron().getName());
        });
    }

    @Test
    void findByPatronAndBanUntilAfterNotBaned() {
        // Act
        Optional<BanRecord> result = underTest.findByPatronAndBanUntilAfter(patron2, LocalDate.now());

        // Assert
        assertThat(result).isEmpty();
    }
}