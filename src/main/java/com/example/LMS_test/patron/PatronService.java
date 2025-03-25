package com.example.LMS_test.patron;

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

import java.util.List;

@Service
@RequiredArgsConstructor
public class PatronService {
    private static final Logger logger = LoggerFactory.getLogger(PatronService.class);
    private final PatronRepository patronRepository;

    @Cacheable("patrons")
    public List<Patron> getAll() {
        return patronRepository.findAll();
    }

    @Cacheable(value = "patrons", key = "#id")
    public Patron getById(Long id) {
        return patronRepository.findById(id).orElseThrow(() -> {
            logger.warn("Patron with id {} not found", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Patron not found");
        });
    }

    @CacheEvict(value = "patrons", allEntries = true)
    @Transactional
    public Patron create(PatronRequest patronRequest) {
        logger.debug("Creating new patron with name: {}", patronRequest.getName());
        try {
            Patron patron = new Patron(patronRequest.getName(), patronRequest.getContactInfo(), null, null);
            Patron savedPatron = patronRepository.save(patron);
            logger.info("New patron created with ID: {}", savedPatron.getId());
            return savedPatron;
        } catch (DataIntegrityViolationException e) {
            logger.error("Unique constraint violation while saving patron", e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patron already exists");
        } catch (Exception e) {
            logger.error("Unexpected error while creating patron", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred");
        }
    }

    @CacheEvict(value = "patrons", allEntries = true)
    @Transactional
    public Patron update(Long id, PatronRequest patronRequest) {
        logger.debug("Updating patron with ID: {}", id);
        return patronRepository.findById(id).map(patron -> {
            if (!patronRequest.getName().isEmpty()) {
                patron.setName(patronRequest.getName());
            }
            if (!patronRequest.getContactInfo().isEmpty()) {
                patron.setContactInfo(patronRequest.getContactInfo());
            }
            try {
                Patron updatedPatron = patronRepository.save(patron);
                logger.info("Updated patron with ID: {}, new name: {}", id, patronRequest.getName());
                return updatedPatron;
            } catch (DataIntegrityViolationException e) {
                logger.error("Unique constraint violation during update for patron ID: {}", id, e);
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Patron already exists");
            }
        }).orElseThrow(() -> {
            logger.warn("Patron with ID {} not found for update", id);
            return new ResponseStatusException(HttpStatus.NOT_FOUND, "Patron not found");
        });
    }

    @CacheEvict(value = "patrons", allEntries = true)
    public void delete(Long id) {
        logger.debug("Deleting patron with ID: {}", id);
        Patron patron = patronRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Patron not found"));
        patronRepository.delete(patron);
        logger.info("Successfully deleted patron with ID: {}, name: {}", id, patron.getName());
    }
}
